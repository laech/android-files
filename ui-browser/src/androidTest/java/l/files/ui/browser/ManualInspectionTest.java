package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.ExtendedFileSystem;

import static android.os.Environment.getExternalStorageDirectory;
import static android.test.MoreAsserts.assertNotEqual;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.fs.ExtendedFileSystem.UTF_8;

@RunWith(AndroidJUnit4.class)
public final class ManualInspectionTest extends InstrumentationTestCase {

    private final ExtendedFileSystem fs =
            new ExtendedFileSystem(LocalFileSystem.INSTANCE);

    @Test
    public void test() throws Exception {
        Path dir = Path.fromFile(getExternalStorageDirectory()).concat("test");
        fs.createDirs(dir);
        try {
            fs.setLastModifiedTime(dir, NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        fs.createFiles(dir.concat(".nomedia"));
        fs.createFiles(dir.concat("html.html"));
        fs.createFiles(dir.concat("zip.zip"));
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
                fs.copy(in, fs, file);
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
            fs.deleteRecursive(dir);
        } catch (FileNotFoundException ignored) {
        }

        fs.createDir(dir);
        fs.createFile(child);
    }

    private void createFutureFiles(Path dir) throws IOException {
        fs.setLastModifiedTime(
                fs.createFiles(dir.concat("future")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(365)));

        fs.setLastModifiedTime(
                fs.createFiles(dir.concat("future3")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(2)));

        fs.setLastModifiedTime(
                fs.createFiles(dir.concat("future5")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + SECONDS.toMillis(5)));
    }

}
