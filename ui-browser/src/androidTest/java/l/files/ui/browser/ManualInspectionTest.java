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
import l.files.fs.local.LocalPath;
import l.files.testing.fs.ExtendedPath;

import static android.os.Environment.getExternalStorageDirectory;
import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.base.Charsets.UTF_8;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

@RunWith(AndroidJUnit4.class)
public final class ManualInspectionTest extends InstrumentationTestCase {

    @Test
    public void test() throws Exception {
        ExtendedPath dir = ExtendedPath.wrap(LocalPath.fromFile(
                getExternalStorageDirectory()).concat("test"));
        dir.createDirs();
        try {
            dir.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        dir.concat(".nomedia").createFiles();
        dir.concat("html.html").createFiles();
        dir.concat("zip.zip").createFiles();
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
            ExtendedPath file = dir.concat(res);
            if (file.exists(NOFOLLOW)) {
                continue;
            }

            InputStream in = getInstrumentation().getContext().getAssets().open(res);
            try {
                file.copy(in);
            } finally {
                in.close();
            }
        }

    }

    private void createNonUtf8Dir() throws IOException {

        byte[] nonUtf8 = {-19, -96, -67, -19, -80, -117};
        assertNotEqual(nonUtf8.clone(), new String(nonUtf8.clone(), UTF_8).getBytes(UTF_8));

        Path dir = LocalPath.fromFile(getExternalStorageDirectory()).concat(nonUtf8);
        Path child = dir.concat("good we can see this dir");

        try {
            ExtendedPath.wrap(dir).deleteRecursive();
        } catch (FileNotFoundException ignored) {
        }

        dir.createDir();
        child.createFile();
    }

    private void createFutureFiles(ExtendedPath dir) throws IOException {
        dir.concat("future")
                .createFiles()
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(365)));

        dir.concat("future3")
                .createFiles()
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(2)));

        dir.concat("future5")
                .createFiles()
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + SECONDS.toMillis(5)));
    }

}
