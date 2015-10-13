package l.files.features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Stream;
import l.files.fs.local.LocalFile;
import l.files.testing.BaseFilesActivityTest;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class AutoRefreshStressTest extends BaseFilesActivityTest {

    public void test_pressing_back_releases_resources() throws Exception {

        File limitFile = LocalFile.of("/proc/sys/fs/inotify/max_user_watches");
        int maxUserWatches = parseInt(limitFile.readAllUtf8().trim());

        File dir = linkToExternalDir("files-test-pressing-back-releases-resources");
        List<File> childDirs = createRandomDirs(dir, 5_000);

        for (int i = 0; i * childDirs.size() < maxUserWatches * 10; i++) {
            screen().clickInto(dir);
            sleep(100);
            screen().pressBack();
        }
        screen().clickInto(dir)
                .assertShowingLatestChildrenDetailsOf(dir)
                .pressBack()
                .clickInto(dir)
                .assertShowingLatestChildrenDetailsOf(dir);
    }

    public void test_can_show_updated_details_of_lots_of_child_dirs() throws Exception {
        int childrenCount = 10_000;
        File dir = linkToExternalDir("files-test-lots-of-child-dirs");
        List<File> childDirs = createRandomDirs(dir, childrenCount);

        screen().clickInto(dir).assertShowingLatestChildrenDetailsOf(dir);

        for (File child : childDirs) {
            Instant time = Instant.ofMillis(currentTimeMillis() - 1000);
            child.setLastModifiedTime(NOFOLLOW, time);
        }

        screen().assertShowingLatestChildrenDetailsOf(dir);
    }

    private List<File> createRandomDirs(File dir, int count) throws IOException {
        List<File> dirs = new ArrayList<>();
        try (Stream<File> children = dir.list(FOLLOW)) {
            for (File child : children) {
                if (child.stat(NOFOLLOW).isDirectory()) {
                    dirs.add(child);
                }
            }
        }
        while (dirs.size() < count) {
            dirs.add(randomFile(dir).createDir());
        }
        return dirs;
    }

    public void test_can_detect_files_added_and_removed_while_loading_() throws Exception {

        int childrenCount = 5000;
        File dir = linkToExternalDir("files-test-add-remove-while-loading");
        List<File> children = createRandomChildren(dir, childrenCount);

        screen().click(dir);

        for (int i = 0; i < 40; i++) {
            while (children.size() > childrenCount) {
                children.remove(children.size() - 1).delete();
            }
            randomFile(dir).createDir();
            randomFile(dir).createFile();
            sleep(100);
        }

        screen().assertShowingLatestChildrenDetailsOf(dir);
    }

    private File linkToExternalDir(String name) throws IOException {
        return dir().resolve(name).createLink(
                externalStorageDir()
                        .resolve(name)
                        .createDirs()
        );
    }

    private List<File> createRandomChildren(File dir, int count) throws IOException {
        List<File> children = dir.list(FOLLOW).to(new ArrayList<File>());
        while (children.size() < count) {
            File file = randomFile(dir);
            children.add(children.size() % 2 == 0
                    ? file.createFile()
                    : file.createDir());
        }
        return children;
    }

    private File randomFile(File dir) {
        return dir.resolve(String.valueOf(Math.random()));
    }

    private File externalStorageDir() {
        return LocalFile.create(getExternalStorageDirectory());
    }

    public void test_shows_correct_information_on_large_change_events() throws Exception {
        dir().resolve("a").createFile();
        screen().assertShowingLatestChildrenDetailsOf(dir());

        long end = currentTimeMillis() + SECONDS.toMillis(10);
        while (currentTimeMillis() < end) {
            updatePermissions("a");
            updateFileContent("b");
            updateDirectoryChild("c");
            updateLink("d");
            updateDirectory("e");
            updateAttributes();
        }

        screen().assertShowingLatestChildrenDetailsOf(dir());
    }

    private void updateAttributes() throws IOException {
        try (Stream<File> stream = dir().list(NOFOLLOW)) {
            for (File child : stream) {
                Random r = new Random();
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
