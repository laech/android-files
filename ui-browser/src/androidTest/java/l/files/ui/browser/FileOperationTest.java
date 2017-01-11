package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import l.files.fs.Path;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.Tests.timeout;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class FileOperationTest extends BaseFilesActivityTest {

    @Test
    public void delete() throws Exception {

        final Path file = fs.createFile(dir().concat("file"));
        final Path link = fs.createSymbolicLink(dir().concat("link"), file);
        final Path dir1 = fs.createDir(dir().concat("dir1"));
        final Path dir2 = fs.createDir(dir().concat("dir2"));
        fs.createFile(dir2.concat("a"));

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
                assertFalse(fs.exists(file, NOFOLLOW));
                assertFalse(fs.exists(link, NOFOLLOW));
                assertFalse(fs.exists(dir1, NOFOLLOW));
                assertFalse(fs.exists(dir2, NOFOLLOW));
            }
        });

    }

    @Test
    public void cut_files() throws Exception {

        final Path file = fs.createFile(dir().concat("a"));
        final Path dir = fs.createDir(dir().concat("dir"));

        screen()
                .longClick(file)
                .cut()
                .click(dir)
                .paste();

        timeout(5, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(fs.exists(file, NOFOLLOW));
                assertTrue(fs.exists(dir.concat(file), NOFOLLOW));
            }
        });

    }

    @Test
    public void copy() throws Exception {

        Path dstDir = fs.createDir(dir().concat("dstDir"));
        Path srcFile = fs.createFile(dir().concat("srcFile"));
        Path srcLink = fs.createSymbolicLink(dir().concat("srcLink"), srcFile);
        Path srcEmpty = fs.createDir(dir().concat("srcEmpty"));
        Path srcFull = fs.createDir(dir().concat("srcFull"));
        fs.createFile(srcFull.concat("a"));
        fs.createDir(srcFull.concat("b"));
        fs.createSymbolicLink(srcFull.concat("c"), srcFull.concat("a"));

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
            if (fs.exists(file, NOFOLLOW)) {
                return true;
            }
            sleep(20);
        }
        return false;
    }

    @Test
    public void paste_menu_is_disabled_inside_folder_being_copied()
            throws Exception {

        Path dir = fs.createDir(dir().concat("dir"));

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
