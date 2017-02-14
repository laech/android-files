package l.files.fs;

import com.google.common.base.Strings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.AlreadyExist;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;
import l.files.testing.fs.PathBaseTest;

import static linux.Limits.NAME_MAX;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

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
    public void access_denied_failure_due_to_no_write_permission_at_parent() throws Exception {
        Path path = dir1().concat("a");
        assertTrue(new File(dir1().toString()).setWritable(false));
        createExpectingFailure(path, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_due_to_no_execute_permission_at_ancestor() throws Exception {
        Path parent = dir1().concat("sub");
        assertTrue(new File(parent.toString()).mkdir());
        assertTrue(new File(dir1().toString()).setExecutable(false));

        Path path = parent.concat("a");
        createExpectingFailure(path, AccessDenied.class);
    }

    @Test
    public void already_exists_failure_due_to_file_exists_at_path() throws Exception {
        Path path = dir1().concat("a").createFile();
        createExpectingFailure(path, AlreadyExist.class);
    }

    @Test
    public void already_exists_failure_due_to_directory_exists_at_path() throws Exception {
        assumeThat(creation, not(PathCreation.DIRECTORIES));
        Path path = dir1().concat("a").createDirectory();
        createExpectingFailure(path, AlreadyExist.class);
    }

    @Test
    public void already_exists_failure_due_to_symbolic_link_exists_at_path() throws Exception {
        Path path = dir1().concat("a").createSymbolicLink(dir2());
        createExpectingFailure(path, AlreadyExist.class);
    }

    @Test
    public void too_many_symbolic_links_failure_due_to_loop() throws Exception {
        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        createExpectingFailure(loop.concat("sub"), TooManySymbolicLinks.class);
    }

    @Test
    public void name_too_long_failure() throws Exception {
        Path path = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        createExpectingFailure(path, NameTooLong.class);
    }

    @Test
    public void no_such_entry_failure_due_to_parent_does_not_exist() throws Exception {
        assumeThat(creation, not(PathCreation.DIRECTORIES));
        Path path = dir1().concat("non-existent").concat("child");
        createExpectingFailure(path, NoSuchEntry.class);
    }

    @Test
    public void not_directory_failure_due_to_parent_not_directory() throws Exception {
        Path parent = dir1().concat("file").createFile();
        createExpectingFailure(parent.concat("a"), NotDirectory.class);
    }

    private void createExpectingFailure(
            Path path,
            Class<? extends IOException> expected
    ) throws IOException {
        try {
            creation.createUsingOurCodeAssertResult(path);
            fail("Expecting " + expected.getName());
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }
}
