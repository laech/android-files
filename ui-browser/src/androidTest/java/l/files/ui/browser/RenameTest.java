package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;
import l.files.fs.Permission;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.fs.Files.deleteRecursiveIfExists;
import static l.files.testing.fs.Files.removePermissions;
import static l.files.ui.browser.Tests.timeout;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RenameTest extends BaseFilesActivityTest {

    // TODO test click ok from keyboard

    @Test
    public void can_rename_to_same_name_but_difference_casing() throws Exception {

        Path dir;
        try {
            dir = createCaseInsensitiveFileSystemDir("can_rename_to_same_name_but_difference_casing");
        } catch (CannotRenameFileToDifferentCasing e) {
            Log.i("RenameTest", "skipping test_can_rename_to_same_name_but_difference_casing()", e);
            return;
        }

        try {

            Path src = fs.createFile(dir.concat("file.txt"));
            Path dst = dir.concat("file.TXT");

            UiRename ui = rename(src);
            ui.setFilename(dst.name().toString());

            sleep(50); // Wait for it to finish checking file existence

            ui.assertHasNoError()
                    .assertOkButtonEnabled(true)
                    .ok()
                    .assertAllItemsDisplayedInOrder(dst);

        } finally {
            deleteRecursiveIfExists(fs, dir);
        }
    }

    @Test
    public void shows_error_when_failed_to_rename() throws Exception {
        Path file = fs.createFile(dir().concat("a"));
        removePermissions(fs, dir(), Permission.write());
        rename(file)
                .setFilename("abc")
                .okExpectingFailure("Permission denied");
    }

    @Test
    public void renames_file_to_specified_name() throws Throwable {
        final Path from = fs.createFile(dir().concat("a"));
        final Path to = dir().concat("abc");

        rename(from).setFilename(to.name().toString()).ok();

        timeout(10, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(fs.exists(from, NOFOLLOW));
                assertTrue(fs.exists(to, NOFOLLOW));
            }
        });
    }

    @Test
    public void highlights_file_base_name_in_dialog() throws Exception {
        Path file = fs.createFile(dir().concat("abc.txt"));
        rename(file).assertSelection("abc");
    }

    @Test
    public void uses_filename_as_default_text() throws Exception {
        Path file = fs.createFile(dir().concat("a"));
        rename(file).assertFilename(file.name().toString());
    }

    @Test
    public void disables_ok_button_with_no_error_initially_because_we_use_source_filename_as_suggestion()
            throws Exception {
        rename(fs.createDir(dir().concat("a")))
                .assertOkButtonEnabled(false)
                .assertHasNoError();
    }

    @Test
    public void cannot_rename_if_new_name_exists() throws Exception {
        fs.createFile(dir().concat("abc"));
        rename(fs.createFile(dir().concat("a")))

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

        Path f1 = fs.createDir(dir().concat("dir"));
        Path f2 = fs.createFile(dir().concat("a"));

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
