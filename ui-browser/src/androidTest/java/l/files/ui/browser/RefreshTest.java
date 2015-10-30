package l.files.ui.browser;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Stream;
import l.files.fs.local.LocalFile;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.MODIFIED;

public final class RefreshTest extends BaseFilesActivityTest {

    @Test
    public void manual_refresh_enabled_if_max_watches_reached()
            throws Exception {

        File dir = linkToCacheDir("files-test-max-watches-reached");
        createRandomDirs(dir, maxUserWatches() + 1);

        screen()
                .sort()
                .by(MODIFIED)
                .clickInto(dir)
                .assertListMatchesFileSystem(dir)
                .assertRefreshMenuVisible(true);

        testRefreshInManualMode(dir);
        testFileCreationDeletionWillStillBeNotifiedInManualMode(dir);
    }

    private void testRefreshInManualMode(File dir) throws IOException {
        try (Stream<File> children = dir.listDirs(FOLLOW)) {
            File childDir = children.iterator().next();
            try {
                // Inotify don't notify child directory last modified time,
                // unless we explicitly monitor the child dir, but we aren't
                // doing that because we ran out of watches, so this is a
                // good operation for testing the manual refresh
                setRandomLastModified(childDir);
            } catch (IOException e) {
                // Older versions does not support changing mtime
                childDir.deleteRecursive();
            }
        }
        screen().refresh().assertListMatchesFileSystem(dir);
    }

    private void testFileCreationDeletionWillStillBeNotifiedInManualMode(File dir)
            throws IOException {

        dir.resolve("file-" + nanoTime()).createFile();
        dir.resolve("dir-" + nanoTime()).createDir();
        dir.resolve("before-move-" + nanoTime()).createFile()
                .moveTo(dir.resolve("after-move-" + nanoTime()));

        try (Stream<File> children = dir.list(FOLLOW)) {
            children.iterator().next().deleteRecursive();
        }

        screen().assertListMatchesFileSystem(dir);
    }

    @Test
    public void manual_refresh_disabled_if_max_watches_not_reached()
            throws Exception {

        screen().assertRefreshMenuVisible(false);
    }

    private int maxUserWatches() throws IOException {
        File limitFile = LocalFile.of("/proc/sys/fs/inotify/max_user_watches");
        return parseInt(limitFile.readAllUtf8().trim());
    }

    @Test
    public void auto_show_updated_details_of_lots_of_child_dirs() throws Exception {
        File dir = linkToCacheDir("files-test-lots-of-child-dirs");
        int count = max(maxUserWatches() / 2, 1000);
        createRandomDirs(dir, count);

        screen().clickInto(dir).assertListMatchesFileSystem(dir);

        try (Stream<File> children = dir.list(FOLLOW)) {
            for (File child : children) {
                if (nanoTime() % 2 == 0) {
                    deleteOrCreateChild(child);
                } else {
                    try {
                        setRandomLastModified(child);
                    } catch (IOException e) {
                        // Older versions does not support changing mtime
                        deleteOrCreateChild(child);
                    }
                }
            }
        }

        screen().assertListMatchesFileSystem(dir);
    }

    private void deleteOrCreateChild(File dir) throws IOException {
        File child = dir.resolve("a");
        try {
            child.delete();
        } catch (FileNotFoundException e) {
            child.createFiles();
        }
    }

    private static final Random random = new Random();

    private void setRandomLastModified(File file) throws IOException {
        long time = random.nextLong();
        file.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(time));
    }

    private void createRandomDirs(File dir, int expectedCount) throws IOException {

        int actualCount = 0;

        try (Stream<File> children = dir.listDirs(FOLLOW)) {
            for (File child : children) {
                actualCount++;
                if (actualCount > expectedCount) {
                    child.deleteRecursive();
                    actualCount--;
                }
            }
        }

        while (actualCount < expectedCount) {
            randomFile(dir).createDir();
            actualCount++;
        }

    }

    @Test
    public void auto_detect_files_added_and_removed_while_loading() throws Exception {

        int childrenCount = (int) (maxUserWatches() * 0.75F);
        File dir = linkToCacheDir("files-test-add-remove-while-loading");
        createRandomChildren(dir, childrenCount);

        screen().click(dir);

        for (int i = 0; i < 200; i++) {
            try (Stream<File> children = dir.list(FOLLOW)) {
                Iterator<File> it = children.iterator();
                it.next().deleteRecursive();
                it.next().deleteRecursive();
            }
            randomFile(dir).createDir();
            randomFile(dir).createFile();
            sleep(10);
        }

        screen().assertListMatchesFileSystem(dir);
    }

    private File linkToCacheDir(String name) throws IOException {
        return dir().resolve(name).createLink(
                cacheDir()
                        .resolve(name)
                        .createDirs()
        );
    }

    private void createRandomChildren(File dir, int expectedCount) throws IOException {
        int actualCount = 0;
        try (Stream<File> children = dir.list(FOLLOW)) {
            for (File ignored : children) {
                actualCount++;
            }
        }
        while (actualCount < expectedCount) {
            File file = randomFile(dir);
            if (actualCount % 2 == 0) {
                file.createFile();
            } else {
                file.createDir();
            }
            actualCount++;
        }
    }

    private File randomFile(File dir) {
        return dir.resolve(String.valueOf(Math.random()));
    }

    private File cacheDir() {
        return LocalFile.of(getInstrumentation().getContext().getExternalCacheDir());
    }

    @Test
    public void auto_show_correct_information_on_large_change_events() throws Exception {
        dir().resolve("a").createFile();
        screen().assertListMatchesFileSystem(dir());

        long end = currentTimeMillis() + SECONDS.toMillis(3);
        while (currentTimeMillis() < end) {
            updatePermissions("a");
            updateFileContent("b");
            updateDirectoryChild("c");
            updateLink("d");
            updateDirectory("e");
            updateAttributes();
        }

        screen().assertListMatchesFileSystem(dir());
    }

    private void updateAttributes() throws IOException {
        try (Stream<File> stream = dir().list(NOFOLLOW)) {
            Random r = new Random();
            for (File child : stream) {
                child.setLastModifiedTime(NOFOLLOW, Instant.of(
                        r.nextInt((int) (currentTimeMillis() / 1000)),
                        r.nextInt(999999)));
            }
        }
    }

    private void updateDirectory(String name) throws IOException {
        File dir = dir().resolve(name);
        if (dir.exists(NOFOLLOW)) {
            dir.delete();
        } else {
            dir.createDir();
        }
    }

    private void updatePermissions(String name) throws IOException {
        File res = dir().resolve(name).createFiles();
        if (res.isReadable()) {
            res.setPermissions(Permission.read());
        } else {
            res.setPermissions(Permission.none());
        }
    }

    private void updateFileContent(String name) throws IOException {
        File file = dir().resolve(name).createFiles();
        file.writeAllUtf8(String.valueOf(new Random().nextLong()));
    }

    private void updateDirectoryChild(String name) throws IOException {
        File dir = dir().resolve(name).createDirs();
        File child = dir.resolve("child");
        if (child.exists(NOFOLLOW)) {
            child.delete();
        } else {
            child.createFile();
        }
    }

    private void updateLink(String name) throws IOException {
        File link = dir().resolve(name);
        if (link.exists(NOFOLLOW)) {
            link.delete();
        }
        link.createLink(new Random().nextInt() % 2 == 0
                ? link
                : link.parent());
    }
}
