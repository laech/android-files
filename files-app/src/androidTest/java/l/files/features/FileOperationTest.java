package l.files.features;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import l.files.fs.Resource;
import l.files.test.BaseFilesActivityTest;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class FileOperationTest extends BaseFilesActivityTest
{

    // TODO cut/delete tests

    public void test_copies_files() throws Exception
    {
        final Resource a = directory().resolve("a").createFile();
        final Resource d = directory().resolve("d").createDirectory();

        screen()
                .toggleSelection(a)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(directory().resolve("d/a"), 5, SECONDS));
    }

    public void test_copies_empty_directory() throws Exception
    {
        final Resource c = directory().resolve("c").createDirectory();
        final Resource d = directory().resolve("d").createDirectory();

        screen()
                .toggleSelection(c)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(directory().resolve("d/c"), 5, SECONDS));
    }

    public void test_copies_full_directory() throws Exception
    {
        final Resource d = directory().resolve("d").createDirectory();
        final Resource c = directory().resolve("c").createDirectory();
        c.resolve("a").createFile();
        c.resolve("b").createDirectory();
        c.resolve("c").createLink(c.resolve("a"));

        screen()
                .toggleSelection(c)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(directory().resolve("d/c"), 5, SECONDS));
    }

    public void test_copies_link() throws Exception
    {
        final Resource d = directory().resolve("d").createDirectory();
        final Resource c = directory().resolve("c").createLink(directory());

        screen()
                .toggleSelection(c)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(directory().resolve("d/c"), 5, SECONDS));
    }

    private boolean waitFor(
            final Resource resource,
            final int time,
            final TimeUnit unit) throws InterruptedException, IOException
    {
        final long end = currentTimeMillis() + unit.toMillis(time);
        while (currentTimeMillis() < end)
        {
            if (resource.exists(NOFOLLOW))
            {
                return true;
            }
            sleep(20);
        }
        return false;
    }

    public void test_paste_menu_is_disabled_inside_folder_being_copied()
            throws Exception
    {
        final Resource dir = directory().resolve("dir").createDirectory();

        screen()
                .toggleSelection(dir)
                .copy()
                .assertCanPaste(true)
                .click(dir)
                .assertCanPaste(false)
                .pressBack()
                .assertCanPaste(true);
    }

    public void test_paste_menu_is_disabled_if_files_do_not_exist()
            throws Exception
    {
        final Resource dir = directory().resolve("dir").createDirectory();

        screen()
                .toggleSelection(dir)
                .copy()
                .assertCanPaste(true);

        dir.delete();

        screen().assertCanPaste(false);
    }

    public void test_paste_menu_is_enabled_if_some_files_do_not_exist_some_exist()
            throws Exception
    {
        final Resource dir = directory().resolve("dir1").createDirectory();
        directory().resolve("dir2").createDirectory();

        screen()
                .toggleSelection(dir)
                .copy()
                .assertCanPaste(true);

        dir.delete();

        screen().assertCanPaste(true);
    }

}
