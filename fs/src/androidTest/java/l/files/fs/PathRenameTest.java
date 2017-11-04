package l.files.fs;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import android.support.annotation.Nullable;

import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.DirectoryNotEmpty;
import l.files.fs.exception.InvalidArgument;
import l.files.fs.exception.IsDirectory;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.fs.Paths.removePermissions;
import static linux.Limits.NAME_MAX;

public final class PathRenameTest extends PathBaseTest {

    @Test
    public void rename_symbolic_link_to_non_existing_destination_will_succeed() throws Exception {
        testRenameFromSymbolicLink(null);
    }

    @Test
    public void rename_symbolic_link_will_replace_existing_destination_symbolic_link() throws Exception {
        testRenameFromSymbolicLink(PathCreation.SYMBOLIC_LINK);
    }

    @Test
    public void rename_symbolic_link_will_replace_existing_destination_file() throws Exception {
        testRenameFromSymbolicLink(PathCreation.FILE);
    }

    private void testRenameFromSymbolicLink(
            @Nullable PathCreation destinationCreationBeforeRename
    ) throws Exception {

        Path target = dir2();
        Path sourceLink = dir1().concat("source").createSymbolicLink(dir2());
        Path destinationLink = dir2().concat("destination");

        if (destinationCreationBeforeRename != null) {
            destinationCreationBeforeRename
                    .createUsingOurCodeAssertResult(destinationLink);
        }

        sourceLink.rename(destinationLink);
        assertEquals(target, destinationLink.readSymbolicLink());
        assertFalse(sourceLink.exists(NOFOLLOW));
    }

    @Test
    public void rename_file_to_non_existing_destination_will_succeed() throws Exception {
        testRenameFromFile(null);
    }

    @Test
    public void rename_file_will_replace_existing_destination_symbolic_link() throws Exception {
        testRenameFromFile(PathCreation.SYMBOLIC_LINK);
    }

    @Test
    public void rename_file_will_replace_existing_destination_file() throws Exception {
        testRenameFromFile(PathCreation.FILE);
    }

    private void testRenameFromFile(
            @Nullable PathCreation destinationCreationBeforeRename
    ) throws Exception {

        String content = "hello world?\r\nhi\n你好、\n";
        Path sourceFile = dir1().concat("source").createFile();
        Paths.writeUtf8(sourceFile, content);
        Path destinationFile = dir2().concat("destination");

        if (destinationCreationBeforeRename != null) {
            destinationCreationBeforeRename
                    .createUsingOurCodeAssertResult(destinationFile);
        }

        sourceFile.rename(destinationFile);
        assertEquals(content, Paths.readAllUtf8(destinationFile));
        assertFalse(sourceFile.exists(NOFOLLOW));
    }

    @Test
    public void rename_directory_to_non_existing_destination_will_succeed() throws Exception {
        testRenameFromDirectory(null);
    }

    @Test
    public void rename_directory_will_replace_existing_empty_destination() throws Exception {
        testRenameFromDirectory(PathCreation.DIRECTORY);
    }

    private void testRenameFromDirectory(
            @Nullable PathCreation destinationCreationBeforeRename
    ) throws Exception {

        Path sourceDirectory = dir1().concat("source").createDirectory();
        Paths.writeUtf8(sourceDirectory.concat("file"), "hello");
        Path destinationDirectory = dir2().concat("destination");

        if (destinationCreationBeforeRename != null) {
            destinationCreationBeforeRename
                    .createUsingOurCodeAssertResult(destinationDirectory);
        }

        sourceDirectory.rename(destinationDirectory);
        assertEquals("hello", Paths.readAllUtf8(destinationDirectory.concat("file")));
        assertFalse(sourceDirectory.exists(NOFOLLOW));
    }

    @Test
    public void not_directory_failure_when_parent_of_source_path_is_not_directory() throws Exception {
        Path source = dir1().concat("file").createFile().concat("invalid");
        Path destination = dir2().concat("destination");
        renameWillFail(source, destination, NotDirectory.class);
    }

    @Test
    public void not_directory_failure_when_parent_of_destination_path_is_not_directory() throws Exception {
        Path source = dir1().concat("file").createFile();
        Path destination = dir2().concat("file").createFile().concat("invalid");
        renameWillFail(source, destination, NotDirectory.class);
    }

    @Test
    public void not_directory_failure_when_source_is_directory_but_destination_is_file() throws Exception {
        Path source = dir1().concat("directory").createDirectory();
        Path destination = dir2().concat("file").createFile();
        renameWillFail(source, destination, NotDirectory.class);
    }

    @Test
    public void not_directory_failure_when_source_is_directory_but_destination_is_symbolic_link() throws Exception {
        Path source = dir1().concat("directory").createDirectory();
        Path destination = dir2().concat("link").createSymbolicLink(Path.of("/"));
        renameWillFail(source, destination, NotDirectory.class);
    }

    @Test
    public void invalid_argument_failure_when_trying_to_make_a_directory_a_subdirectory_of_itself() throws Exception {
        Path parent = dir1().concat("parent").createDirectory();
        Path child = parent.concat("child").createDirectory();
        renameWillFail(parent, child, InvalidArgument.class);
    }

    @Test
    public void is_directory_failure_if_new_path_is_directory_but_old_path_is_file() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        Path file = dir1().concat("file").createFile();
        renameWillFail(file, dir, IsDirectory.class);
    }

    @Test
    public void is_directory_failure_if_new_path_is_directory_but_old_path_is_symbolic_link() throws Exception {
        Path dir = dir1().concat("dir").createDirectory();
        Path link = dir1().concat("file").createSymbolicLink(dir2());
        renameWillFail(link, dir, IsDirectory.class);
    }

    @Test
    public void too_many_symbolic_links_failure_when_old_path_has_loop() throws Exception {
        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        Path source = loop.concat("source");
        renameWillFail(source, dir2(), TooManySymbolicLinks.class);
    }

    @Test
    public void too_many_symbolic_links_failure_when_new_path_has_loop() throws Exception {
        Path loop = dir1().concat("loop");
        loop.createSymbolicLink(loop);
        Path destination = loop.concat("destination");
        renameWillFail(dir2(), destination, TooManySymbolicLinks.class);
    }

    @Test
    public void name_too_long_failure_if_old_path_name_too_long() throws Exception {
        Path nameTooLong = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        renameWillFail(nameTooLong, dir2(), NameTooLong.class);
    }

    @Test
    public void name_too_long_failure_if_new_path_name_too_long() throws Exception {
        Path nameTooLong = dir1().concat(Strings.repeat("a", NAME_MAX + 1));
        renameWillFail(dir2(), nameTooLong, NameTooLong.class);
    }

    @Test
    public void no_such_entry_failure_if_source_does_not_exist() throws Exception {
        Path missing = dir1().concat("missing");
        renameWillFail(missing, dir2(), NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_destination_parent_does_not_exist() throws Exception {
        renameWillFail(dir1(), dir2().concat("missing/dir"), NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_source_is_empty() throws Exception {
        renameWillFail(Path.of(""), dir1(), NoSuchEntry.class);
    }

    @Test
    public void no_such_entry_failure_if_destination_is_empty() throws Exception {
        renameWillFail(dir1(), Path.of(""), NoSuchEntry.class);
    }

    @Test
    public void directory_not_empty_failure_if_source_is_file_destination_is_non_empty_directory() throws Exception {
        dir2().concat("a").createFile();
        renameWillFail(dir1(), dir2(), DirectoryNotEmpty.class);
    }

    @Test
    public void directory_not_empty_failure_if_source_is_symbolic_link_destination_is_non_empty_directory() throws Exception {
        dir2().concat("a").createSymbolicLink(Path.of("/"));
        renameWillFail(dir1(), dir2(), DirectoryNotEmpty.class);
    }

    @Test
    public void directory_not_empty_failure_if_source_is_directory_destination_is_non_empty_directory() throws Exception {
        dir2().concat("a").createDirectory();
        renameWillFail(dir1(), dir2(), DirectoryNotEmpty.class);
    }

    @Test
    public void access_denied_failure_when_no_write_permission_on_old_parent() throws Exception {
        Path oldPath = dir1().concat("old-file").createFile();
        Path newPath = dir2().concat("new-file");

        removePermissions(oldPath.parent(), Permission.write());
        assertWritable(oldPath.parent(), false);
        assertWritable(newPath.parent(), true);
        assertSearchable(oldPath.parent(), true);
        assertSearchable(newPath.parent(), true);

        renameWillFail(oldPath, newPath, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_when_no_write_permission_on_new_parent() throws Exception {
        Path oldPath = dir1().concat("old-file").createFile();
        Path newPath = dir2().concat("new-file");

        removePermissions(newPath.parent(), Permission.write());
        assertWritable(oldPath.parent(), true);
        assertWritable(newPath.parent(), false);
        assertSearchable(oldPath.parent(), true);
        assertSearchable(newPath.parent(), true);

        renameWillFail(oldPath, newPath, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_when_no_search_permission_on_old_parent() throws Exception {
        Path oldPath = dir1().concat("old-file").createFile();
        Path newPath = dir2().concat("new-file");

        removePermissions(oldPath.parent(), Permission.execute());
        assertSearchable(oldPath.parent(), false);
        assertSearchable(newPath.parent(), true);
        assertWritable(oldPath.parent(), true);
        assertWritable(newPath.parent(), true);

        renameWillFail(oldPath, newPath, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_when_no_search_permission_on_new_parent() throws Exception {
        Path oldPath = dir1().concat("old-file").createFile();
        Path newPath = dir2().concat("new-file");

        removePermissions(newPath.parent(), Permission.execute());
        assertSearchable(oldPath.parent(), true);
        assertSearchable(newPath.parent(), false);
        assertWritable(oldPath.parent(), true);
        assertWritable(newPath.parent(), true);

        renameWillFail(oldPath, newPath, AccessDenied.class);
    }

    @Test
    public void access_denied_failure_when_old_path_is_directory_with_no_write_permission() throws Exception {
        Path oldPath = dir1().concat("old-dir").createDirectory();
        Path newPath = dir2().concat("new-file");

        removePermissions(oldPath, Permission.write());
        assertWritable(oldPath, false);
        assertWritable(oldPath.parent(), true);
        assertWritable(newPath.parent(), true);
        assertSearchable(oldPath.parent(), true);
        assertSearchable(newPath.parent(), true);

        renameWillFail(oldPath, newPath, AccessDenied.class);
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

    private static void renameWillFail(
            Path oldPath,
            Path newPath,
            Class<? extends IOException> expected
    ) throws IOException {

        try {
            oldPath.rename(newPath);
            fail("Expected " + expected.getName());
        } catch (IOException e) {
            if (!expected.isInstance(e)) {
                throw e;
            }
        }
    }

}
