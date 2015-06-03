package l.files.features;

import l.files.R;
import l.files.common.base.Executable;
import l.files.features.objects.UiRename;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.test.BaseFilesActivityTest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.timeout;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class RenameTest extends BaseFilesActivityTest
{

    public void test_shows_error_when_failed_to_rename() throws Exception
    {
        final Resource file = directory().resolve("a").createFile();
        directory().removePermissions(Permission.write());
        rename(file)
                .setFilename("abc")
                .okExpectingFailure("Permission denied");
    }

    public void test_renames_file_to_specified_name() throws Throwable
    {
        final Resource from = directory().resolve("a").createFile();
        final Resource to = directory().resolve("abc");

        rename(from).setFilename(to.name()).ok();

        timeout(1, SECONDS, new Executable()
        {
            @Override
            public void execute() throws Exception
            {
                assertFalse(from.exists(NOFOLLOW));
                assertTrue(to.exists(NOFOLLOW));
            }
        });
    }

    public void test_highlights_file_base_name_in_dialog() throws Exception
    {
        final Resource file = directory().resolve("abc.txt").createFile();
        rename(file).assertSelection("abc");
    }

    public void test_uses_filename_as_default_text() throws Exception
    {
        final Resource file = directory().resolve("a").createFile();
        rename(file).assertFilename(file.name());
    }

    public void test_disables_ok_button_with_no_error_initially_because_we_use_source_filename_as_suggestion()
            throws Exception
    {
        rename(directory().resolve("a").createDirectory())
                .assertOkButtonEnabled(false)
                .assertHasNoError();
    }

    public void test_cannot_rename_if_new_name_exists() throws Exception
    {
        directory().resolve("abc").createFile();
        rename(directory().resolve("a").createFile())

                .setFilename("abc")
                .assertOkButtonEnabled(false)
                .assertHasError(R.string.name_exists)

                .setFilename("ab")
                .assertOkButtonEnabled(true)
                .assertHasNoError();
    }

    public void test_rename_button_is_disable_if_there_are_more_than_one_file_checked()
            throws Exception
    {
        final Resource f1 = directory().resolve("dir").createDirectory();
        final Resource f2 = directory().resolve("a").createFile();

        screen()
                .check(f1, true)
                .check(f2, true)
                .assertCanRename(false)

                .check(f1, false)
                .assertCanRename(true);
    }

    private UiRename rename(final Resource resource)
    {
        return screen().check(resource, true).rename();
    }
}
