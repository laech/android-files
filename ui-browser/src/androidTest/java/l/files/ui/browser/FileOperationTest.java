package l.files.ui.browser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import l.files.fs.File;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.Tests.timeout;

public final class FileOperationTest extends BaseFilesActivityTest {

    public void test_delete_file() throws Exception {

        final File a = dir().resolve("a").createFile();

        screen()
                .longClick(a)
                .delete()
                .ok();

        timeout(5, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(a.exists(NOFOLLOW));
            }
        });

    }

    public void test_cut_files() throws Exception {

        final File file = dir().resolve("a").createFile();
        final File dir = dir().resolve("dir").createDir();

        screen()
                .longClick(file)
                .cut()
                .click(dir)
                .paste();

        timeout(5, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(file.exists(NOFOLLOW));
                assertTrue(dir.resolve(file.name()).exists(NOFOLLOW));
            }
        });

    }

    public void test_copy_files() throws Exception {

        final File file = dir().resolve("a").createFile();
        final File dir = dir().resolve("dir").createDir();

        screen()
                .longClick(file)
                .copy()
                .click(dir)
                .paste();

        timeout(5, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertTrue(dir.resolve(file.name()).exists(NOFOLLOW));
                assertTrue(file.exists(NOFOLLOW));
            }
        });
    }

    public void test_copies_empty_directory() throws Exception {

        File c = dir().resolve("c").createDir();
        File d = dir().resolve("d").createDir();

        screen()
                .longClick(c)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(dir().resolve("d/c"), 5, SECONDS));
    }

    public void test_copies_full_directory() throws Exception {

        File d = dir().resolve("d").createDir();
        File c = dir().resolve("c").createDir();
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

        File d = dir().resolve("d").createDir();
        File c = dir().resolve("c").createLink(dir());

        screen()
                .longClick(c)
                .copy()
                .click(d)
                .paste();

        assertTrue(waitFor(dir().resolve("d/c"), 5, SECONDS));
    }

    private boolean waitFor(
            File file,
            int time,
            TimeUnit unit) throws InterruptedException, IOException {

        long end = currentTimeMillis() + unit.toMillis(time);
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

        File dir = dir().resolve("dir").createDir();

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

}
