package l.files.ui.browser;

import android.util.Log;
import l.files.testing.fs.Paths;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static java.lang.Thread.sleep;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.testing.Tests.timeout;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RenameTest extends BaseFilesActivityTest {

    // TODO test click ok from keyboard

    @Test
    public void can_rename_to_same_name_but_difference_casing()
        throws Exception {

        Path dir;
        try {
            dir = createCaseInsensitiveFileSystemDir(
                "can_rename_to_same_name_but_difference_casing");
        } catch (CannotRenameFileToDifferentCasing e) {
            Log.i(
                "RenameTest",
                "skipping test_can_rename_to_same_name_but_difference_casing()",
                e
            );
            return;
        }

        try {

            Path src = createFile(dir.resolve("file.txt"));
            Path dst = dir.resolve("file.TXT");

            UiRename ui = rename(src);
            ui.setFilename(dst.getFileName().toString());

            sleep(50); // Wait for it to finish checking file existence

            ui.assertHasNoError()
                .assertOkButtonEnabled(true)
                .ok()
                .assertAllItemsDisplayedInOrder(dst);

        } finally {
            Paths.deleteRecursiveIfExists(l.files.fs.Path.of(dir));
        }
    }

    @Test
    public void shows_error_when_failed_to_rename() throws Exception {
        Path file = createFile(dir().resolve("a"));
        Paths.removePermissions(
            l.files.fs.Path.of(dir()),
            PosixFilePermissions.fromString("-w--w--w-")
        );
        rename(file)
            .setFilename("abc")
            .okExpectingFailure(".+AccessDeniedException.+$");
    }

    @Test
    public void renames_file_to_specified_name() throws Throwable {
        Path from = createFile(dir().resolve("a"));
        Path to = dir().resolve("abc");

        rename(from).setFilename(to.getFileName().toString()).ok();

        timeout(10, SECONDS, () -> {
            assertFalse(exists(from, NOFOLLOW_LINKS));
            assertTrue(exists(to, NOFOLLOW_LINKS));
        });
    }

    @Test
    public void highlights_file_base_name_in_dialog() throws Exception {
        Path file = createFile(dir().resolve("abc.txt"));
        rename(file).assertSelection("abc");
    }

    @Test
    public void uses_filename_as_default_text() throws Exception {
        Path file = createFile(dir().resolve("a"));
        rename(file).assertFilename(file.getFileName().toString());
    }

    @Test
    public void disables_ok_button_with_no_error_initially_because_we_use_source_filename_as_suggestion()
        throws Exception {
        rename(createDirectory(dir().resolve("a")))
            .assertOkButtonEnabled(false)
            .assertHasNoError();
    }

    @Test
    public void cannot_rename_if_new_name_exists() throws Exception {
        createFile(dir().resolve("abc"));
        rename(createFile(dir().resolve("a")))

            .setFilename("abc")
            .assertOkButtonEnabled(false)
            .assertHasError(R.string.name_exists)

            .setFilename("ab")
            .assertOkButtonEnabled(true)
            .assertHasNoError();
    }

    @Test
    public void rename_button_is_disable_if_there_are_more_than_one_file_checked()
        throws Exception {

        Path f1 = createDirectory(dir().resolve("dir"));
        Path f2 = createFile(dir().resolve("a"));

        screen()
            .longClick(f1)
            .click(f2)
            .assertCanRename(false)

            .click(f1)
            .assertCanRename(true);
    }

    private UiRename rename(Path file) {
        return screen().longClick(file).rename();
    }
}
