package l.files.fs;

import com.google.common.base.Strings;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.DirectoryNotEmpty;
import l.files.fs.exception.InvalidArgument;
import l.files.fs.exception.IsDirectory;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.TooManySymbolicLinks;
import l.files.testing.fs.PathBaseTest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static l.files.testing.fs.Paths.removePermissions;
import static linux.Limits.NAME_MAX;

public final class PathMoveTest extends PathBaseTest {

    @Test
    public void invalid_argument_failure_when_trying_to_make_a_directory_a_subdirectory_of_itself()
            throws Exception {

        Path parent = dir1().concat("parent").createDirectory();
        Path child = parent.concat("child").createDirectory();
        moveWillFail(parent, child, InvalidArgument.class);
    }

    @Test
    public void is_directory_failure_if_new_path_is_directory_but_old_path_is_not_directory()
            throws Exception {

        Path dir = dir1().concat("dir").createDirectory();
        Path file = dir1().concat("file").createFile();
        moveWillFail(file, dir, IsDirectory.class);
    }

    @Test
    public void too_many_symbolic_links_failure_when_old_path_has_loop()
            throws Exception {

        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        Path source = loop.concat("source");
        moveWillFail(source, dir2(), TooManySymbolicLinks.class);
    }

    @Test
    public void too_many_symbolic_links_failure_when_new_path_has_loop()
            throws Exception {

        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        Path destination = loop.concat("destination");
        moveWillFail(dir2(), destination, TooManySymbolicLinks.class);
    }

    @Test
    public void name_too_long_failure_if_old_path_name_too_long()
            throws Exception {

        Path nameTooLong = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        moveWillFail(nameTooLong, dir2(), NameTooLong.class);
    }

    @Test
    public void name_too_long_failure_if_new_path_name_too_long()
            throws Exception {

        Path nameTooLong = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        moveWillFail(dir2(), nameTooLong, NameTooLong.class);
    }

    @Test
    public void no_such_entry_failure_if_source_does_not_exist()
            throws Exception {

        Path missing = dir1().concat("missing");
        moveWillFail(missing, dir2(), NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_destination_parent_does_not_exist()
            throws Exception {

        moveWillFail(dir1(), dir2().concat("missing/dir"), NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_source_is_empty()
            throws Exception {

        moveWillFail(Path.of(""), dir1(), NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_destination_is_empty()
            throws Exception {

        moveWillFail(dir1(), Path.of(""), NoSuchEntry.class);
    }

    @Test
    public void directory_not_empty_failure_if_destination_is_non_empty_directory()
            throws Exception {

        dir2().concat("a").createFile();
        moveWillFail(dir1(), dir2(), DirectoryNotEmpty.class);
    }

    @Test
    public void access_denied_failure_when_no_write_permission_on_old_parent()
            throws Exception {

        Path oldPath = dir1().concat("old-file").createFile();
        Path newPath = dir2().concat("new-file");

        removePermissions(oldPath.parent(), Permission.write());
        assertWritable(oldPath.parent(), false);
        assertWritable(newPath.parent(), true);
        assertSearchable(oldPath.parent(), true);
        assertSearchable(newPath.parent(), true);

        moveWillFail(oldPath, newPath, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_when_no_write_permission_on_new_parent()
            throws Exception {

        Path oldPath = dir1().concat("old-file").createFile();
        Path newPath = dir2().concat("new-file");

        removePermissions(newPath.parent(), Permission.write());
        assertWritable(oldPath.parent(), true);
        assertWritable(newPath.parent(), false);
        assertSearchable(oldPath.parent(), true);
        assertSearchable(newPath.parent(), true);

        moveWillFail(oldPath, newPath, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_when_no_search_permission_on_old_parent()
            throws Exception {

        Path oldPath = dir1().concat("old-file").createFile();
        Path newPath = dir2().concat("new-file");

        removePermissions(oldPath.parent(), Permission.execute());
        assertSearchable(oldPath.parent(), false);
        assertSearchable(newPath.parent(), true);
        assertWritable(oldPath.parent(), true);
        assertWritable(newPath.parent(), true);

        moveWillFail(oldPath, newPath, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_when_no_search_permission_on_new_parent()
            throws Exception {

        Path oldPath = dir1().concat("old-file").createFile();
        Path newPath = dir2().concat("new-file");

        removePermissions(newPath.parent(), Permission.execute());
        assertSearchable(oldPath.parent(), true);
        assertSearchable(newPath.parent(), false);
        assertWritable(oldPath.parent(), true);
        assertWritable(newPath.parent(), true);

        moveWillFail(oldPath, newPath, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_when_old_path_is_directory_with_no_write_permission()
            throws Exception {

        Path oldPath = dir1().concat("old-dir").createDirectory();
        Path newPath = dir2().concat("new-file");

        removePermissions(oldPath, Permission.write());
        assertWritable(oldPath, false);
        assertWritable(oldPath.parent(), true);
        assertWritable(newPath.parent(), true);
        assertSearchable(oldPath.parent(), true);
        assertSearchable(newPath.parent(), true);

        moveWillFail(oldPath, newPath, AccessDenied.class);
    }

    private static void assertWritable(Path path, boolean writable) {
        assertEquals(
                path.toString() + " is writable?",
                writable,
                new File(path.toString()).canWrite()
        );
    }

    private static void assertSearchable(Path path, boolean searchable) {
        assertEquals(
                path.toString() + " is searchable?",
                searchable,
                new File(path.toString()).canExecute()
        );
    }

    private static void moveWillFail(
            Path oldPath,
            Path newPath,
            Class<? extends IOException> expected
    ) throws IOException {

        try {
            oldPath.move(newPath);
            fail("Expected " + expected.getName());
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }
}
