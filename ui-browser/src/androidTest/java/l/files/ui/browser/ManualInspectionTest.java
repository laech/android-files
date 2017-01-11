package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import l.files.fs.FileSystem;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.Files;

import static android.os.Environment.getExternalStorageDirectory;
import static android.test.MoreAsserts.assertNotEqual;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.fs.Files.UTF_8;

@RunWith(AndroidJUnit4.class)
public final class ManualInspectionTest extends InstrumentationTestCase {

    private final FileSystem fs = LocalFileSystem.INSTANCE;

    @Test
    public void test() throws Exception {
        Path dir = Path.fromFile(getExternalStorageDirectory()).concat("test");
        Files.createDirs(fs, dir);
        try {
            fs.setLastModifiedTime(dir, NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        Files.createFiles(fs, dir.concat(".nomedia"));
        Files.createFiles(fs, dir.concat("html.html"));
        Files.createFiles(fs, dir.concat("zip.zip"));
        try {
            createNonUtf8Dir();
        } catch (IOException e) {
            // Emulator not supported
        }

        try {
            createFutureFiles(dir);
        } catch (IOException ignored) {
            // Older versions does not support changing mtime
        }

        List<String> resources = asList(
                "will_scale_up.jpg",
                "will_scale_down.jpg",
                "test.pdf",
                "test.mp4",
                "test.m4a",
                "test.svg");

        for (String res : resources) {
            Path file = dir.concat(res);
            if (fs.exists(file, NOFOLLOW)) {
                continue;
            }

            InputStream in = getInstrumentation().getContext().getAssets().open(res);
            try {
                Files.copy(in, fs, file);
            } finally {
                in.close();
            }
        }

    }

    private void createNonUtf8Dir() throws IOException {

        byte[] nonUtf8 = {-19, -96, -67, -19, -80, -117};
        assertNotEqual(nonUtf8.clone(), new String(nonUtf8.clone(), UTF_8).getBytes(UTF_8));

        Path dir = Path.fromFile(getExternalStorageDirectory()).concat(nonUtf8);
        Path child = dir.concat("good we can see this dir");

        try {
            Files.deleteRecursive(fs, dir);
        } catch (FileNotFoundException ignored) {
        }

        fs.createDir(dir);
        fs.createFile(child);
    }

    private void createFutureFiles(Path dir) throws IOException {
        fs.setLastModifiedTime(
                Files.createFiles(fs, dir.concat("future")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(365)));

        fs.setLastModifiedTime(
                Files.createFiles(fs, dir.concat("future3")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(2)));

        fs.setLastModifiedTime(
                Files.createFiles(fs, dir.concat("future5")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + SECONDS.toMillis(5)));
    }

}
