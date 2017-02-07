package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.AlreadyExist;
import l.files.testing.fs.PathBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public final class PathCreateFailureTest extends PathBaseTest {

    private final PathCreation creation;

    public PathCreateFailureTest(PathCreation creation) {
        this.creation = creation;
    }

    @Parameters(name = "{0}")
    public static Iterable<PathCreation[]> data() {
        return PathCreation.valuesAsJUnitParameters();
    }

    @Test
    public void access_denied_failure_due_to_no_write_permission_at_parent()
            throws Exception {

        Path path = dir1().concat("a");
        assertTrue(new File(dir1().toString()).setWritable(false));
        creationFailureAccessDenied(path);
    }

    @Test
    public void access_denied_failure_due_to_no_execute_permission_at_ancestor()
            throws Exception {

        Path parent = dir1().concat("sub");
        assertTrue(new File(parent.toString()).mkdir());
        assertTrue(new File(dir1().toString()).setExecutable(false));

        Path path = parent.concat("a");
        creationFailureAccessDenied(path);
    }

    private void creationFailureAccessDenied(Path path) throws IOException {
        try {
            creation.createUsingOurCodeAssertResult(path);
            fail("Expecting " + AccessDenied.class.getName());
        } catch (AccessDenied e) {
            // Pass
        }
    }

    @Test
    public void already_exists_failure_due_to_file_exists_at_path() throws Exception {

        Path path = dir1().concat("a").createFile();
        creationFailureAlreadyExists(path);
    }

    @Test
    public void already_exists_failure_due_to_directory_exists_at_path()
            throws Exception {

        Path path = dir1().concat("a").createDirectory();
        creationFailureAlreadyExists(path);
    }

    @Test
    public void already_exists_failure_due_to_symbolic_link_exists_at_path()
            throws Exception {

        Path path = dir1().concat("a").createSymbolicLink(dir2());
        creationFailureAlreadyExists(path);
    }

    private void creationFailureAlreadyExists(Path path) throws IOException {
        assertTrue(path.exists(NOFOLLOW));
        try {
            creation.createUsingOurCodeAssertResult(path);
            fail("Expecting " + AlreadyExist.class.getName());
        } catch (AlreadyExist e) {
            // Pass
        }
    }

}
