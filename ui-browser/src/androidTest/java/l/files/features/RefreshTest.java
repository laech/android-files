package l.files.features;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import l.files.common.base.Executable;
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Stream;
import l.files.fs.local.LocalFile;
import l.files.testing.BaseFilesActivityTest;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.Tests.timeout;

public final class RefreshTest extends BaseFilesActivityTest {

    public void test_manual_refresh_enabled_if_max_watches_reached()
            throws Exception {

        File linkedDir = linkToDirWithMaxWatchReached();

        screen()
                .clickInto(linkedDir)
                .assertListMatchesFileSystem(linkedDir)
                .assertRefreshMenuVisible(true);

        randomFile(linkedDir).createDir();
        screen().refresh().assertListMatchesFileSystem(linkedDir);
    }

    public void test_manual_refresh_enabled_but_still_auto_refreshes_in_app_cut()
            throws Exception {

        final File linkedDir = linkToDirWithMaxWatchReached();
        final File src = dir().resolve("cut-" + nanoTime()).createFile();
        final File dst = linkedDir.resolve(src.name());

        assertFalse(dst.exists(NOFOLLOW));

        screen()
                .longClick(src)
                .cut()
                .clickInto(linkedDir)
                .assertListMatchesFileSystem(linkedDir)
                .assertRefreshMenuVisible(true)
                .paste();


        timeout(10, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertTrue(dst.exists(NOFOLLOW));
            }
        });

        screen().assertListMatchesFileSystem(linkedDir);
    }

    public void test_manual_refresh_enabled_but_still_auto_refreshes_in_app_copy()
            throws Exception {

        final File linkedDir = linkToDirWithMaxWatchReached();
        final File src = dir().resolve("copy-" + nanoTime()).createFile();
        final File dst = linkedDir.resolve(src.name());

        assertFalse(dst.exists(NOFOLLOW));

        screen()
                .longClick(src)
                .copy()
                .clickInto(linkedDir)
                .assertListMatchesFileSystem(linkedDir)
                .assertRefreshMenuVisible(true)
                .paste();

        timeout(10, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertTrue(dst.exists(NOFOLLOW));
            }
        });

        screen().assertListMatchesFileSystem(linkedDir);

    }

    public void test_manual_refresh_enabled_but_still_auto_refreshes_in_app_delete()
            throws Exception {

        final File linkedDir = linkToDirWithMaxWatchReached();
        final File src = linkedDir.resolve("delete-" + nanoTime()).createFile();

        assertTrue(src.exists(NOFOLLOW));

        screen()
                .clickInto(linkedDir)
                .assertListMatchesFileSystem(linkedDir)
                .assertListViewContains(src, true)
                .assertRefreshMenuVisible(true)
                .longClick(src)
                .delete()
                .ok();

        timeout(10, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertFalse(src.exists(NOFOLLOW));
            }
        });

        screen().assertListMatchesFileSystem(linkedDir);

    }

    public void test_manual_refresh_enabled_but_still_auto_refreshes_in_app_rename()
            throws Exception {

        final File linkedDir = linkToDirWithMaxWatchReached();
        final File src = linkedDir.resolve("before-rename-" + nanoTime()).createFile();
        final File dst = linkedDir.resolve("after--rename-" + nanoTime());

        assertFalse(dst.exists(NOFOLLOW));

        screen()
                .clickInto(linkedDir)
                .assertListMatchesFileSystem(linkedDir)
                .assertListViewContains(src, true)
                .assertRefreshMenuVisible(true)
                .longClick(src)
                .rename()
                .setFilename(dst.name())
                .ok();

        timeout(10, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertTrue(dst.exists(NOFOLLOW));
            }
        });

        screen().assertListMatchesFileSystem(linkedDir);

    }

    public void test_manual_refresh_enabled_but_still_auto_refreshes_in_app_new_dir()
            throws Exception {

        final File linkedDir = linkToDirWithMaxWatchReached();
        final File dst = linkedDir.resolve("new-dir-" + nanoTime());

        assertFalse(dst.exists(NOFOLLOW));

        screen()
                .clickInto(linkedDir)
                .assertListMatchesFileSystem(linkedDir)
                .assertRefreshMenuVisible(true)
                .newFolder()
                .setFilename(dst.name())
                .ok();

        timeout(10, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertTrue(dst.exists(NOFOLLOW));
            }
        });

        screen().assertListMatchesFileSystem(linkedDir);
    }

    private File linkToDirWithMaxWatchReached() throws IOException {
        File dir = linkToExternalDir("files-test-max-watches-reached");
        createRandomDirs(dir, maxUserWatches() + 1);
        return dir;
    }

    public void test_manual_refresh_disabled_if_max_watches_not_reached()
            throws Exception {

        screen().assertRefreshMenuVisible(false);
    }

    public void test_pressing_back_releases_resources() throws Exception {

        int maxUserWatches = maxUserWatches();
        File dir = linkToExternalDir("files-test-pressing-back-releases-resources");
        createRandomDirs(dir, (int) (maxUserWatches * 0.75F));

        for (int i = 0; i < 10; i++) {
            screen().clickInto(dir);
            sleep(100);
            screen().pressBack();
        }
        screen().clickInto(dir)
                .assertListMatchesFileSystem(dir)
                .pressBack()
                .clickInto(dir)
                .assertListMatchesFileSystem(dir);
    }

    private int maxUserWatches() throws IOException {
        File limitFile = LocalFile.of("/proc/sys/fs/inotify/max_user_watches");
        return parseInt(limitFile.readAllUtf8().trim());
    }

    public void test_auto_show_updated_details_of_lots_of_child_dirs() throws Exception {
        File dir = linkToExternalDir("files-test-lots-of-child-dirs");
        createRandomDirs(dir, maxUserWatches() - 2);

        screen().clickInto(dir).assertListMatchesFileSystem(dir);

        try (Stream<File> children = dir.list(FOLLOW)) {
            for (File child : children) {
                if (nanoTime() % 2 == 0) {
                    deleteOrCreateChild(child);
                } else {
                    setRandomLastModified(child);
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
            child.createFile();
        }
    }

    private void setRandomLastModified(File file) throws IOException {
        long time = ThreadLocalRandom.current().nextLong();
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

    public void test_auto_detect_files_added_and_removed_while_loading() throws Exception {

        int childrenCount = (int) (maxUserWatches() * 0.75F);
        File dir = linkToExternalDir("files-test-add-remove-while-loading");
        createRandomChildren(dir, childrenCount);

        screen().click(dir);

        for (int i = 0; i < 40; i++) {
            try (Stream<File> children = dir.list(FOLLOW)) {
                Iterator<File> it = children.iterator();
                it.next().deleteRecursive();
                it.next().deleteRecursive();
            }
            randomFile(dir).createDir();
            randomFile(dir).createFile();
            sleep(100);
        }

        screen().assertListMatchesFileSystem(dir);
    }

    private File linkToExternalDir(String name) throws IOException {
        return dir().resolve(name).createLink(
                externalStorageDir()
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

    private File externalStorageDir() {
        return LocalFile.of(getExternalStorageDirectory());
    }

    public void test_auto_show_correct_information_on_large_change_events() throws Exception {
        dir().resolve("a").createFile();
        screen().assertListMatchesFileSystem(dir());

        long end = currentTimeMillis() + SECONDS.toMillis(10);
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
                child.setLastAccessedTime(NOFOLLOW, Instant.of(
                        r.nextInt((int) (currentTimeMillis() / 1000)),
                        r.nextInt(999999)));
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
