package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;

import l.files.fs.exception.AlreadyExist;
import l.files.testing.fs.PathBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public final class PathCreateFailureAlreadyExistsTest extends PathBaseTest {

    private final PathCreation creation;

    public PathCreateFailureAlreadyExistsTest(PathCreation creation) {
        this.creation = creation;
    }

    @Parameters(name = "{0}")
    public static Iterable<PathCreation[]> data() {
        return PathCreation.valuesAsJUnitParameters();
    }

    @Test
    public void create_failure_due_to_file_exists_at_path() throws Exception {

        Path path = dir1().concat("a").createFile();
        creationFailureAlreadyExists(path);
    }

    @Test
    public void create_failure_due_to_directory_exists_at_path()
            throws Exception {

        Path path = dir1().concat("a").createDirectory();
        creationFailureAlreadyExists(path);
    }

    @Test
    public void create_failure_due_to_symbolic_link_exists_at_path()
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
