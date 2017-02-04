package l.files.ui.browser;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.testing.fs.Paths;

import static android.os.Environment.getExternalStorageDirectory;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.base.Charsets.UTF_8;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ManualInspectionTest {

    @Test
    public void test() throws Exception {
        Path dir = Path.create(getExternalStorageDirectory()).concat("test");
        dir.createDirectories();
        try {
            dir.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        Paths.createFiles(dir.concat(".nomedia"));
        Paths.createFiles(dir.concat("html.html"));
        Paths.createFiles(dir.concat("zip.zip"));
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
            if (file.exists(NOFOLLOW)) {
                continue;
            }

            InputStream in = getInstrumentation().getContext().getAssets().open(res);
            try {
                Paths.copy(in, file);
            } finally {
                in.close();
            }
        }

    }

    private void createNonUtf8Dir() throws IOException {

        byte[] nonUtf8 = {-19, -96, -67, -19, -80, -117};
        assertNotEqual(nonUtf8.clone(), new String(nonUtf8.clone(), UTF_8).getBytes(UTF_8));

        Path dir = Path.create(getExternalStorageDirectory()).concat(nonUtf8);
        Path child = dir.concat("good we can see this dir");

        try {
            Paths.deleteRecursive(dir);
        } catch (FileNotFoundException ignored) {
        }

        dir.createDirectory();
        child.createFile();
    }

    private void createFutureFiles(Path dir) throws IOException {
        Paths.createFiles(dir.concat("future"))
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(365)));

        Paths.createFiles(dir.concat("future3"))
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(2)));

        Paths.createFiles(dir.concat("future5"))
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + SECONDS.toMillis(5)));
    }

}
