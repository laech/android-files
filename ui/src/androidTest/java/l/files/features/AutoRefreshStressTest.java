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
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class AutoRefreshStressTest extends BaseFilesActivityTest {

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

        screen().assertListViewContainsChildrenOf(dir);
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
        screen().assertListViewContainsChildrenOf(dir());

        long end = currentTimeMillis() + SECONDS.toMillis(10);
        while (currentTimeMillis() < end) {
            updatePermissions("a");
            updateFileContent("b");
            updateDirectoryChild("c");
            updateLink("d");
            updateDirectory("e");
            updateAttributes();
        }

        screen().assertListViewContainsChildrenOf(dir());
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
