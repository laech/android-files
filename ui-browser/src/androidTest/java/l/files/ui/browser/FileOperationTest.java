package l.files.ui.browser;

import org.junit.Test;

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

public final class FileOperationTest extends BaseFilesActivityTest {

    @Test
    public void delete() throws Exception {

        final Path file = Files.createFile(dir().resolve("file"));
        final Path link = Files.createLink(dir().resolve("link"), file);
        final Path dir1 = Files.createDir(dir().resolve("dir1"));
        final Path dir2 = Files.createDir(dir().resolve("dir2"));
        Files.createFile(dir2.resolve("a"));

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

        final Path file = Files.createFile(dir().resolve("a"));
        final Path dir = Files.createDir(dir().resolve("dir"));

        screen()
                .longClick(file)
                .cut()
                .click(dir)
                .paste();

        timeout(5, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(Files.exists(file, NOFOLLOW));
                assertTrue(Files.exists(dir.resolve(file.name()), NOFOLLOW));
            }
        });

    }

    @Test
    public void copy() throws Exception {

        Path dstDir = Files.createDir(dir().resolve("dstDir"));
        Path srcFile = Files.createFile(dir().resolve("srcFile"));
        Path srcLink = Files.createLink(dir().resolve("srcLink"), srcFile);
        Path srcEmpty = Files.createDir(dir().resolve("srcEmpty"));
        Path srcFull = Files.createDir(dir().resolve("srcFull"));
        Files.createFile(srcFull.resolve("a"));
        Files.createDir(srcFull.resolve("b"));
        Files.createLink(srcFull.resolve("c"), srcFull.resolve("a"));

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

        Path dir = Files.createDir(dir().resolve("dir"));

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
