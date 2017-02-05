package l.files.fs;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Collection;

import l.files.fs.exception.AlreadyExist;
import l.files.testing.fs.PathBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class PathCreateFailureAlreadyExistsTest extends PathBaseTest {

    private final String subPath;

    public PathCreateFailureAlreadyExistsTest(String subPath) {
        this.subPath = subPath;
    }

    abstract PathCreation creation();

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {""},
                {"."},
                {".."},
                {"abc"},
                {" "},
                {"\n"},
                {"\t"},
        });
    }

    @Test
    public void create_failure_due_to_file_exists_at_path() throws Exception {

        Path path = dir1().concat(subPath);
        if (!path.exists(NOFOLLOW)) {
            path.createFile();
        }

        creationFailureAlreadyExists(path);
    }

    @Test
    public void create_failure_due_to_directory_exists_at_path()
            throws Exception {

        Path path = dir1().concat(subPath);
        if (!path.exists(NOFOLLOW)) {
            path.createDirectory();
        }

        creationFailureAlreadyExists(path);
    }

    @Test
    public void create_failure_due_to_symbolic_link_exists_at_path()
            throws Exception {

        Path path = dir1().concat(subPath);
        if (!path.exists(NOFOLLOW)) {
            path.createSymbolicLink(dir2());
        }

        creationFailureAlreadyExists(path);
    }

    private void creationFailureAlreadyExists(Path path) throws IOException {
        assertTrue(path.exists(NOFOLLOW));
        try {
            creation().createUsingOurCode(path);
            fail("Expecting " + AlreadyExist.class.getName());
        } catch (IOException e) {
            assertEquals(AlreadyExist.class, e.getClass());
        }
    }

}
