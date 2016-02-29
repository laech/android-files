package l.files.ui.browser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import l.files.fs.FileSystem;
import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.Permission;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createDirs;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createFiles;
import static l.files.fs.Files.createSymbolicLink;
import static l.files.fs.Files.deleteRecursive;
import static l.files.fs.Files.list;
import static l.files.fs.Files.listDirs;
import static l.files.fs.Files.move;
import static l.files.fs.Files.readAllUtf8;
import static l.files.fs.Files.setLastModifiedTime;
import static l.files.fs.Files.writeUtf8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.FileSort.MODIFIED;

public final class RefreshTest extends BaseFilesActivityTest {

    public void test_manual_refresh_updates_outdated_files()
            throws Exception {

        Path dir = linkToStorageDir("files-test-max-watches-reached");
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

    private void testRefreshInManualMode(Path dir) throws IOException {

        listDirs(dir, FOLLOW, new FileSystem.Consumer<Path>() {
            @Override
            public boolean accept(Path childDir) throws IOException {
                try {
                    // Inotify don't notify child directory last modified time,
                    // unless we explicitly monitor the child dir, but we aren't
                    // doing that because we ran out of watches, so this is a
                    // good operation for testing the manual refresh
                    setRandomLastModified(childDir);
                } catch (IOException e) {
                    // Older versions does not support changing mtime
                    deleteRecursive(childDir);
                }
                return false;
            }
        });
        screen().refresh().assertListMatchesFileSystem(dir);
    }

    private void testFileCreationDeletionWillStillBeNotifiedInManualMode(Path dir)
            throws IOException {

        createFile(dir.resolve("file-" + nanoTime()));
        createDir(dir.resolve("dir-" + nanoTime()));
        move(createFile(dir.resolve("before-move-" + nanoTime())),
                dir.resolve("after-move-" + nanoTime()));

        list(dir, FOLLOW, new FileSystem.Consumer<Path>() {
            @Override
            public boolean accept(Path file) throws IOException {
                deleteRecursive(file);
                return false;
            }
        });

        screen().assertListMatchesFileSystem(dir);
    }

    public void test_manual_refresh_disabled_if_max_watches_not_reached()
            throws Exception {

        screen().assertRefreshMenuVisible(false);
    }

    private int maxUserWatches() throws IOException {
        Path limitFile = Paths.get("/proc/sys/fs/inotify/max_user_watches");
        return parseInt(readAllUtf8(limitFile).trim());
    }

    public void test_auto_show_updated_details_of_lots_of_child_dirs() throws Exception {
        Path dir = linkToStorageDir("files-test-lots-of-child-dirs");
        int count = max(maxUserWatches() / 2, 1000);
        createRandomDirs(dir, count);

        screen().clickInto(dir).assertListMatchesFileSystem(dir);

        list(dir, FOLLOW, new FileSystem.Consumer<Path>() {
            @Override
            public boolean accept(Path child) throws IOException {
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
                return true;
            }
        });

        screen().assertListMatchesFileSystem(dir);
    }

    private void deleteOrCreateChild(Path dir) throws IOException {
        Path child = dir.resolve("a");
        try {
            Files.delete(child);
        } catch (FileNotFoundException e) {
            createFiles(child);
        }
    }

    private static final Random random = new Random();

    private void setRandomLastModified(Path file) throws IOException {
        long time = random.nextLong();
        setLastModifiedTime(file, NOFOLLOW, Instant.ofMillis(time));
    }

    private void createRandomDirs(
            final Path dir,
            final int expectedCount) throws IOException {

        final int[] actualCount = {0};

        listDirs(dir, FOLLOW, new FileSystem.Consumer<Path>() {
            @Override
            public boolean accept(Path child) throws IOException {
                actualCount[0]++;
                if (actualCount[0] > expectedCount) {
                    deleteRecursive(child);
                    actualCount[0]--;
                }
                return true;
            }
        });

        while (actualCount[0] < expectedCount) {
            createDir(randomFile(dir));
            actualCount[0]++;
        }

    }

    public void test_auto_detect_files_added_and_removed_while_loading() throws Exception {

        int childrenCount = (int) (maxUserWatches() * 0.75F);
        Path dir = linkToStorageDir("files-test-add-remove-while-loading");
        createRandomChildren(dir, childrenCount);

        screen().click(dir);

        for (int i = 0; i < 200; i++) {

            list(dir, FOLLOW, new FileSystem.Consumer<Path>() {

                int count = 0;

                @Override
                public boolean accept(Path file) throws IOException {
                    deleteRecursive(file);
                    count++;
                    return count < 2;
                }

            });

            createDir(randomFile(dir));
            createFile(randomFile(dir));
            sleep(10);
        }

        screen().assertListMatchesFileSystem(dir);
    }

    private Path linkToStorageDir(String name) throws IOException {
        return createSymbolicLink(
                dir().resolve(name), createDirs(storageDir().resolve(name))
        );
    }

    private void createRandomChildren(Path dir, int expectedCount) throws IOException {

        final int[] actualCount = {0};
        list(dir, FOLLOW, new FileSystem.Consumer<Path>() {
            @Override
            public boolean accept(Path file) {
                actualCount[0]++;
                return true;
            }
        });

        while (actualCount[0] < expectedCount) {
            Path file = randomFile(dir);
            if (actualCount[0] % 2 == 0) {
                createFile(file);
            } else {
                createDir(file);
            }
            actualCount[0]++;
        }
    }

    private Path randomFile(Path dir) {
        return dir.resolve(String.valueOf(Math.random()));
    }

    private Path storageDir() {
        return Paths.get(getExternalStorageDirectory());
    }

    public void test_auto_show_correct_information_on_large_change_events() throws Exception {
        createFile(dir().resolve("a"));
        screen().assertListMatchesFileSystem(dir());

        long end = currentTimeMillis() + SECONDS.toMillis(5);
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

        final Random r = new Random();
        list(dir(), NOFOLLOW, new FileSystem.Consumer<Path>() {
            @Override
            public boolean accept(Path child) throws IOException {
                setLastModifiedTime(child, NOFOLLOW, Instant.of(
                        r.nextInt((int) (currentTimeMillis() / 1000)),
                        r.nextInt(999999)));
                return true;
            }
        });
    }

    private void updateDirectory(String name) throws IOException {
        Path dir = dir().resolve(name);
        if (Files.exists(dir, NOFOLLOW)) {
            Files.delete(dir);
        } else {
            Files.createDir(dir);
        }
    }

    private void updatePermissions(String name) throws IOException {
        Path res = createFiles(dir().resolve(name));
        if (Files.isReadable(res)) {
            Files.setPermissions(res, Permission.read());
        } else {
            Files.setPermissions(res, Permission.none());
        }
    }

    private void updateFileContent(String name) throws IOException {
        Path file = createFiles(dir().resolve(name));
        writeUtf8(file, String.valueOf(new Random().nextLong()));
    }

    private void updateDirectoryChild(String name) throws IOException {
        Path dir = createDirs(dir().resolve(name));
        Path child = dir.resolve("child");
        if (Files.exists(child, NOFOLLOW)) {
            Files.delete(child);
        } else {
            Files.createFile(child);
        }
    }

    private void updateLink(String name) throws IOException {
        Path link = dir().resolve(name);
        if (Files.exists(link, NOFOLLOW)) {
            Files.delete(link);
        }
        Files.createSymbolicLink(
                link,
                new Random().nextInt() % 2 == 0
                        ? link
                        : link.parent()
        );
    }
}
