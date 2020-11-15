package l.files.ui.browser;

import org.junit.Test;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.testing.Tests.timeout;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class FileOperationTest extends BaseFilesActivityTest {

    @Test
    public void delete() throws Exception {

        Path file = createFile(dir().resolve("file"));
        Path link = createSymbolicLink(dir().resolve("link"), file);
        Path dir1 = createDirectory(dir().resolve("dir1"));
        Path dir2 = createDirectory(dir().resolve("dir2"));
        createFile(dir2.resolve("a"));

        screen()
            .longClick(file)
            .click(link)
            .click(dir1)
            .click(dir2)
            .delete()
            .ok();

        timeout(5, SECONDS, () -> {
            assertFalse(exists(file, NOFOLLOW_LINKS));
            assertFalse(exists(link, NOFOLLOW_LINKS));
            assertFalse(exists(dir1, NOFOLLOW_LINKS));
            assertFalse(exists(dir2, NOFOLLOW_LINKS));
        });

    }

    @Test
    public void cut_files() throws Exception {

        Path file = createFile(dir().resolve("a"));
        Path dir = createDirectory(dir().resolve("dir"));

        screen()
            .longClick(file)
            .cut()
            .click(dir)
            .paste();

        timeout(5, SECONDS, () -> {
            assertFalse(exists(file, NOFOLLOW_LINKS));
            assertTrue(exists(dir.resolve(file.getFileName()), NOFOLLOW_LINKS));
        });

    }

    @Test
    public void copy() throws Exception {

        Path dstDir = createDirectory(dir().resolve("dstDir"));
        Path srcFile = createFile(dir().resolve("srcFile"));
        Path srcLink = createSymbolicLink(dir().resolve("srcLink"), srcFile);
        Path srcEmpty = createDirectory(dir().resolve("srcEmpty"));
        Path srcFull = createDirectory(dir().resolve("srcFull"));
        createFile(srcFull.resolve("a"));
        createDirectory(srcFull.resolve("b"));
        createSymbolicLink(srcFull.resolve("c"), srcFull.resolve("a"));

        screen()
            .longClick(srcEmpty)
            .click(srcFull)
            .click(srcFile)
            .click(srcLink)
            .copy()
            .click(dstDir)
            .paste();

        assertTrue(waitFor(dstDir.resolve(srcFile.getFileName()), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcLink.getFileName()), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcEmpty.getFileName()), 5, SECONDS));
        assertTrue(waitFor(dstDir.resolve(srcFull.getFileName()), 5, SECONDS));
        assertTrue(waitFor(
            dstDir.resolve(srcFull.getFileName()).resolve("a"),
            5,
            SECONDS
        ));
        assertTrue(waitFor(
            dstDir.resolve(srcFull.getFileName()).resolve("b"),
            5,
            SECONDS
        ));
        assertTrue(waitFor(
            dstDir.resolve(srcFull.getFileName()).resolve("c"),
            5,
            SECONDS
        ));
    }

    private boolean waitFor(
        Path file,
        int time,
        TimeUnit unit
    ) throws InterruptedException {

        long end = currentTimeMillis() + unit.toMillis(time);
        while (currentTimeMillis() < end) {
            if (exists(file, NOFOLLOW_LINKS)) {
                return true;
            }
            sleep(20);
        }
        return false;
    }

    @Test
    public void paste_menu_is_disabled_inside_folder_being_copied()
        throws Exception {

        Path dir = createDirectory(dir().resolve("dir"));

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
