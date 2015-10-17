package l.files.ui.browser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import l.files.fs.File;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class FileOperationTest extends BaseFilesActivityTest {

    // TODO cut/delete tests

    public void test_copies_files() throws Exception {
        final File a = dir().resolve("a").createFile();
        final File d = dir().resolve("d").createDir();

        screen()
                .longClick(a)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(dir().resolve("d/a"), 5, SECONDS));
    }

    public void test_copies_empty_directory() throws Exception {
        final File c = dir().resolve("c").createDir();
        final File d = dir().resolve("d").createDir();

        screen()
                .longClick(c)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(dir().resolve("d/c"), 5, SECONDS));
    }

    public void test_copies_full_directory() throws Exception {
        final File d = dir().resolve("d").createDir();
        final File c = dir().resolve("c").createDir();
        c.resolve("a").createFile();
        c.resolve("b").createDir();
        c.resolve("c").createLink(c.resolve("a"));

        screen()
                .longClick(c)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(dir().resolve("d/c"), 5, SECONDS));
    }

    public void test_copies_link() throws Exception {
        final File d = dir().resolve("d").createDir();
        final File c = dir().resolve("c").createLink(dir());

        screen()
                .longClick(c)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(dir().resolve("d/c"), 5, SECONDS));
    }

    private boolean waitFor(
            final File file,
            final int time,
            final TimeUnit unit) throws InterruptedException, IOException {
        final long end = currentTimeMillis() + unit.toMillis(time);
        while (currentTimeMillis() < end) {
            if (file.exists(NOFOLLOW)) {
                return true;
            }
            sleep(20);
        }
        return false;
    }

    public void test_paste_menu_is_disabled_inside_folder_being_copied()
            throws Exception {
        final File dir = dir().resolve("dir").createDir();

        screen()
                .longClick(dir)
                .copy()
                .assertCanPaste(true)
                .clickInto(dir)
                .assertCanPaste(false)
                .pressBack()
                .assertCurrentDirectory(dir())
                .assertCanPaste(true);
    }

    public void test_paste_menu_is_disabled_if_files_do_not_exist()
            throws Exception {
        final File dir = dir().resolve("dir").createDir();

        screen()
                .longClick(dir)
                .copy()
                .assertCanPaste(true);

        dir.delete();

        screen().assertCanPaste(false);
    }

    public void test_paste_menu_is_enabled_if_some_files_do_not_exist_some_exist()
            throws Exception {
        final File dir = dir().resolve("dir1").createDir();
        dir().resolve("dir2").createDir();

        screen()
                .longClick(dir)
                .copy()
                .assertCanPaste(true);

        dir.delete();

        screen().assertCanPaste(true);
    }

}
