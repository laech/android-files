package l.files.fs;

import org.junit.Test;

import l.files.fs.exception.AlreadyExist;
import l.files.testing.fs.PathBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class PathCreateDirectoriesTest extends PathBaseTest {

    @Test
    public void creates_missing_directories() throws Exception {
        dir1().concat("a/b/c").createDirectories();
        assertTrue(dir1().concat("a/b/c").stat(NOFOLLOW).isDirectory());
        assertTrue(dir1().concat("a/b").stat(NOFOLLOW).isDirectory());
        assertTrue(dir1().concat("a/").stat(NOFOLLOW).isDirectory());
    }

    @Test
    public void does_not_error_if_directory_already_exists() throws Exception {
        Path path = dir1().concat("dir");
        path.createDirectory();
        path.createDirectories();
        path.createDirectories();
        assertTrue(path.stat(NOFOLLOW).isDirectory());
    }

    @Test
    public void errors_if_already_exists_but_is_not_directory() throws Exception {
        Path path = dir1().concat("file");
        path.createFile();
        try {
            path.createDirectories();
            fail("Expecting " + AlreadyExist.class.getName());
        } catch (AlreadyExist e) {
            // Pass
        }
        assertTrue(path.stat(NOFOLLOW).isRegularFile());
    }
}
