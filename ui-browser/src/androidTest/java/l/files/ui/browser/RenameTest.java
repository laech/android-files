package l.files.ui.browser;

import org.junit.Test;

import l.files.fs.File;
import l.files.fs.Permission;

import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.Tests.timeout;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class RenameTest extends BaseFilesActivityTest {

    // TODO test can rename from file.ext to file.EXT
    // TODO test click ok from keyboard

    @Test
    public void shows_error_when_failed_to_rename() throws Exception {
        final File file = dir().resolve("a").createFile();
        dir().removePermissions(Permission.write());
        rename(file)
                .setFilename("abc")
                .okExpectingFailure("Permission denied");
    }

    @Test
    public void renames_file_to_specified_name() throws Throwable {
        final File from = dir().resolve("a").createFile();
        final File to = dir().resolve("abc");

        rename(from).setFilename(to.name().toString()).ok();

        timeout(1, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(from.exists(NOFOLLOW));
                assertTrue(to.exists(NOFOLLOW));
            }
        });
    }

    @Test
    public void highlights_file_base_name_in_dialog() throws Exception {
        final File file = dir().resolve("abc.txt").createFile();
        rename(file).assertSelection("abc");
    }

    @Test
    public void uses_filename_as_default_text() throws Exception {
        final File file = dir().resolve("a").createFile();
        rename(file).assertFilename(file.name().toString());
    }

    @Test
    public void disables_ok_button_with_no_error_initially_because_we_use_source_filename_as_suggestion()
            throws Exception {
        rename(dir().resolve("a").createDir())
                .assertOkButtonEnabled(false)
                .assertHasNoError();
    }

    @Test
    public void cannot_rename_if_new_name_exists() throws Exception {
        dir().resolve("abc").createFile();
        rename(dir().resolve("a").createFile())

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
        final File f1 = dir().resolve("dir").createDir();
        final File f2 = dir().resolve("a").createFile();

        screen()
                .longClick(f1)
                .click(f2)
                .assertCanRename(false)

                .click(f1)
                .assertCanRename(true);
    }

    private UiRename rename(final File file) {
        return screen().longClick(file).rename();
    }
}