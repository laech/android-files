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

    public void test_delete() throws Exception {

        final File file = dir().resolve("file").createFile();
        final File link = dir().resolve("link").createLink(file);
        final File dir1 = dir().resolve("dir1").createDir();
        final File dir2 = dir().resolve("dir2").createDir();
        dir2.resolve("a").createFile();

        screen()
                .longClick(file)
                .click(link)
                .click(dir1)
                .click(dir2)
                .delete()
                .ok();

        timeout(5, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(file.exists(NOFOLLOW));
                assertFalse(link.exists(NOFOLLOW));
                assertFalse(dir1.exists(NOFOLLOW));
                assertFalse(dir2.exists(NOFOLLOW));
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

    public void test_copy() throws Exception {

        File dstDir = dir().resolve("dstDir").createDir();
        File srcFile = dir().resolve("srcFile").createFile();
        File srcLink = dir().resolve("srcLink").createLink(srcFile);
        File srcEmpty = dir().resolve("srcEmpty").createDir();
        File srcFull = dir().resolve("srcFull").createDir();
        srcFull.resolve("a").createFile();
        srcFull.resolve("b").createDir();
        srcFull.resolve("c").createLink(srcFull.resolve("a"));

        screen()
                .longClick(srcEmpty)
                .click(srcFull)
                .click(srcFile)
                .click(srcLink)
                .copy()
                .click(dstDir)
                .paste();

        assertTrue(waitFor(dstDir.resolve(srcFile.name()), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcLink.name()), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcEmpty.name()), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcFull.name()), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcFull.name()).resolve("a"), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcFull.name()).resolve("b"), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcFull.name()).resolve("c"), 5, SECONDS));
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
