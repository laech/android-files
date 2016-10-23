package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import l.files.fs.Files;
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

        final Path file = Files.createFile(dir().concat("file"));
        final Path link = Files.createSymbolicLink(dir().concat("link"), file);
        final Path dir1 = Files.createDir(dir().concat("dir1"));
        final Path dir2 = Files.createDir(dir().concat("dir2"));
        Files.createFile(dir2.concat("a"));

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
                assertFalse(Files.exists(file, NOFOLLOW));
                assertFalse(Files.exists(link, NOFOLLOW));
                assertFalse(Files.exists(dir1, NOFOLLOW));
                assertFalse(Files.exists(dir2, NOFOLLOW));
            }
        });

    }

    @Test
    public void cut_files() throws Exception {

        final Path file = Files.createFile(dir().concat("a"));
        final Path dir = Files.createDir(dir().concat("dir"));

        screen()
                .longClick(file)
                .cut()
                .click(dir)
                .paste();

        timeout(5, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(Files.exists(file, NOFOLLOW));
                assertTrue(Files.exists(dir.concat(file.name()), NOFOLLOW));
            }
        });

    }

    @Test
    public void copy() throws Exception {

        Path dstDir = Files.createDir(dir().concat("dstDir"));
        Path srcFile = Files.createFile(dir().concat("srcFile"));
        Path srcLink = Files.createSymbolicLink(dir().concat("srcLink"), srcFile);
        Path srcEmpty = Files.createDir(dir().concat("srcEmpty"));
        Path srcFull = Files.createDir(dir().concat("srcFull"));
        Files.createFile(srcFull.concat("a"));
        Files.createDir(srcFull.concat("b"));
        Files.createSymbolicLink(srcFull.concat("c"), srcFull.concat("a"));

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
            if (Files.exists(file, NOFOLLOW)) {
                return true;
            }
            sleep(20);
        }
        return false;
    }

    @Test
    public void paste_menu_is_disabled_inside_folder_being_copied()
            throws Exception {

        Path dir = Files.createDir(dir().concat("dir"));

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
