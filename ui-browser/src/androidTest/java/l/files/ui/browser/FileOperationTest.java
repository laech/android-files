package l.files.ui.browser;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import l.files.fs.Path;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.Tests.timeout;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class FileOperationTest extends BaseFilesActivityTest {

    @Test
    public void delete() throws Exception {

        Path file = dir().concat("file").createFile();
        Path link = dir().concat("link").createSymbolicLink(file);
        Path dir1 = dir().concat("dir1").createDirectory();
        Path dir2 = dir().concat("dir2").createDirectory();
        dir2.concat("a").createFile();

        screen()
                .longClick(file)
                .click(link)
                .click(dir1)
                .click(dir2)
                .delete()
                .ok();

        timeout(5, SECONDS, () -> {
            assertFalse(file.exists(NOFOLLOW));
            assertFalse(link.exists(NOFOLLOW));
            assertFalse(dir1.exists(NOFOLLOW));
            assertFalse(dir2.exists(NOFOLLOW));
        });

    }

    @Test
    public void cut_files() throws Exception {

        Path file = dir().concat("a").createFile();
        Path dir = dir().concat("dir").createDirectory();

        screen()
                .longClick(file)
                .cut()
                .click(dir)
                .paste();

        timeout(5, SECONDS, () -> {
            assertFalse(file.exists(NOFOLLOW));
            assertTrue(dir.concat(file.name()).exists(NOFOLLOW));
        });

    }

    @Test
    public void copy() throws Exception {

        Path dstDir = dir().concat("dstDir").createDirectory();
        Path srcFile = dir().concat("srcFile").createFile();
        Path srcLink = dir().concat("srcLink").createSymbolicLink(srcFile);
        Path srcEmpty = dir().concat("srcEmpty").createDirectory();
        Path srcFull = dir().concat("srcFull").createDirectory();
        srcFull.concat("a").createFile();
        srcFull.concat("b").createDirectory();
        srcFull.concat("c").createSymbolicLink(srcFull.concat("a"));

        screen()
                .longClick(srcEmpty)
                .click(srcFull)
                .click(srcFile)
                .click(srcLink)
                .copy()
                .click(dstDir)
                .paste();

        assertTrue(waitFor(dstDir.concat(srcFile.name()), 5, SECONDS));
        assertTrue(waitFor(dstDir.concat(srcLink.name()), 5, SECONDS));
        assertTrue(waitFor(dstDir.concat(srcEmpty.name()), 5, SECONDS));
        assertTrue(waitFor(dstDir.concat(srcFull.name()), 5, SECONDS));
        assertTrue(waitFor(dstDir.concat(srcFull.name()).concat("a"), 5, SECONDS));
        assertTrue(waitFor(dstDir.concat(srcFull.name()).concat("b"), 5, SECONDS));
        assertTrue(waitFor(dstDir.concat(srcFull.name()).concat("c"), 5, SECONDS));
    }

    private boolean waitFor(
            Path file,
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

    @Test
    public void paste_menu_is_disabled_inside_folder_being_copied()
            throws Exception {

        Path dir = dir().concat("dir").createDirectory();

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
