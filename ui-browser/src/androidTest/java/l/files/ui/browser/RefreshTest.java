package l.files.ui.browser;

import androidx.test.runner.AndroidJUnit4;
import l.files.fs.Path;
import l.files.fs.Path.Consumer;
import l.files.testing.fs.Paths;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.ui.browser.FilesLoader.BATCH_UPDATE_MILLIS;
import static l.files.ui.browser.sort.FileSort.MODIFIED;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public final class RefreshTest extends BaseFilesActivityTest {

    @Test
    public void manual_refresh_updates_outdated_files()
        throws Exception {

        int watchLimit = 5;
        setActivityIntent(newIntent(dir(), watchLimit));

        for (int i = 0; i < watchLimit + 5; i++) {
            dir().concat(String.valueOf(i)).createDirectory();
        }

        screen()
            .sort()
            .by(MODIFIED)
            .assertListMatchesFileSystem(dir())
            .assertRefreshMenuVisible(true);

        testRefreshInManualMode(dir());
        testFileCreationDeletionWillStillBeNotifiedInManualMode(dir());
    }

    private void testRefreshInManualMode(Path dir) throws IOException {

        Paths.listDirectories(dir, (Consumer) childDir -> {
            // Inotify don't notify child directory last modified time,
            // unless we explicitly monitor the child dir, but we aren't
            // doing that because we ran out of watches, so this is a
            // good operation for testing the manual refresh
            childDir.concat("x").createFile();
            return false;
        });

        boolean updated = false;
        try {
            screen().assertListMatchesFileSystem(
                dir,
                BATCH_UPDATE_MILLIS + 2000,
                MILLISECONDS
            );
            updated = true;
        } catch (AssertionError e) {
            // Pass
        }
        assertFalse("Expected auto refresh to have been disabled", updated);
        screen().refresh().assertListMatchesFileSystem(dir);
    }

    private void testFileCreationDeletionWillStillBeNotifiedInManualMode(Path dir)
        throws IOException {

        dir.concat("file-" + nanoTime()).createFile();
        dir.concat("dir-" + nanoTime()).createDirectory();
        dir.concat("before-move-" + nanoTime())
            .createFile()
            .move(dir.concat("after-move-" + nanoTime()));

        dir.list((Consumer) file -> {
            Paths.deleteRecursive(file);
            return false;
        });

        screen().assertListMatchesFileSystem(dir);
    }

    @Test
    public void auto_detect_files_added_and_removed_while_loading()
        throws Exception {

        for (int i = 0; i < 10; i++) {
            dir().concat(String.valueOf(i)).createDirectory();
        }

        Thread thread = new Thread(() -> {
            long start = currentTimeMillis();
            while (currentTimeMillis() - start < 5000) {
                try {

                    deleteFiles(2);
                    randomFile(dir()).createDirectory();
                    randomFile(dir()).createFile();

                } catch (IOException ignore) {
                }
            }
        });
        thread.start();

        screen();
        thread.join();
        screen().assertListMatchesFileSystem(dir());
    }

    private void deleteFiles(int n) throws IOException {
        dir().list(new Consumer() {

            int count = 0;

            @Override
            public boolean accept(Path file) throws IOException {
                Paths.deleteRecursive(file);
                count++;
                return count < n;
            }

        });
    }

    private Path randomFile(Path dir) {
        return dir.concat(String.valueOf(Math.random()));
    }

    @Test
    public void auto_show_correct_information_on_large_change_events()
        throws Exception {
        dir().concat("a").createFile();
        screen().assertListMatchesFileSystem(dir());

        long end = currentTimeMillis() + SECONDS.toMillis(5);
        while (currentTimeMillis() < end) {
            updatePermissions("a");
            updateFileContent("b");
            updateDirectoryChild("c");
            updateLink("d", "a", "b");
            updateDirectory("e");
            updateAttributes();
        }

        screen().assertListMatchesFileSystem(dir());
    }

    private void updateAttributes() throws IOException {

        Random r = new Random();
        dir().list((Consumer) child -> {
            child.setLastModifiedTime(FileTime.from(Instant.ofEpochSecond(
                r.nextInt((int) (currentTimeMillis() / 1000)),
                r.nextInt(999999)
            )));
            return true;
        });
    }

    private void updateDirectory(String name) throws IOException {
        Path dir = dir().concat(name);
        if (dir.exists(NOFOLLOW_LINKS)) {
            dir.delete();
        } else {
            dir.createDirectory();
        }
    }

    private void updatePermissions(String name) throws IOException {
        Path res = dir().concat(name);
        Paths.createFiles(res);
        if (res.isReadable()) {
            res.setPermissions(PosixFilePermissions.fromString("r--r--r--"));
        } else {
            res.setPermissions(emptySet());
        }
    }

    private void updateFileContent(String name) throws IOException {
        Path file = dir().concat(name);
        Paths.createFiles(file);
        Paths.writeUtf8(file, String.valueOf(new Random().nextLong()));
    }

    private void updateDirectoryChild(String name) throws IOException {
        Path dir = dir().concat(name).createDirectories();
        Path child = dir.concat("child");
        if (child.exists(NOFOLLOW_LINKS)) {
            child.delete();
        } else {
            child.createFile();
        }
    }

    private void updateLink(String name, String target1, String target2)
        throws IOException {
        Path link = dir().concat(name);
        if (link.exists(NOFOLLOW_LINKS)) {
            link.delete();
        }
        link.createSymbolicLink(
            new Random().nextInt() % 2 == 0
                ? dir().concat(target1)
                : dir().concat(target2)
        );
    }
}
