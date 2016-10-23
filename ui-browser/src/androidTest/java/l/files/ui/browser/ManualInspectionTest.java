package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import l.files.fs.Files;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Paths;

import static android.os.Environment.getExternalStorageDirectory;
import static android.test.MoreAsserts.assertNotEqual;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Files.UTF_8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

@RunWith(AndroidJUnit4.class)
public final class ManualInspectionTest extends InstrumentationTestCase {

    @Test
    public void test() throws Exception {
        Path dir = Paths.get(getExternalStorageDirectory()).concat("test");
        Files.createDirs(dir);
        try {
            Files.setLastModifiedTime(dir, NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        Files.createFiles(dir.concat(".nomedia"));
        Files.createFiles(dir.concat("html.html"));
        Files.createFiles(dir.concat("zip.zip"));
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
            if (Files.exists(file, NOFOLLOW)) {
                continue;
            }

            InputStream in = getInstrumentation().getContext().getAssets().open(res);
            try {
                Files.copy(in, file);
            } finally {
                in.close();
            }
        }

    }

    private void createNonUtf8Dir() throws IOException {

        byte[] nonUtf8 = {-19, -96, -67, -19, -80, -117};
        assertNotEqual(nonUtf8.clone(), new String(nonUtf8.clone(), UTF_8).getBytes(UTF_8));

        Path dir = Paths.get(getExternalStorageDirectory()).concat(nonUtf8);
        Path child = dir.concat("good we can see this dir");

        try {
            Files.deleteRecursive(dir);
        } catch (FileNotFoundException ignored) {
        }

        Files.createDir(dir);
        Files.createFile(child);
    }

    private void createFutureFiles(Path dir) throws IOException {
        Files.setLastModifiedTime(
                Files.createFiles(dir.concat("future")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(365)));

        Files.setLastModifiedTime(
                Files.createFiles(dir.concat("future3")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(2)));

        Files.setLastModifiedTime(
                Files.createFiles(dir.concat("future5")),
                FOLLOW,
                Instant.ofMillis(currentTimeMillis() + SECONDS.toMillis(5)));
    }

}
