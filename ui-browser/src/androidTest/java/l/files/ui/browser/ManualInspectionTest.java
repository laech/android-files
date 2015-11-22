package l.files.ui.browser;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import l.files.base.io.Closer;
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.local.LocalFile;

import static android.os.Environment.getExternalStorageDirectory;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.File.UTF_8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertNotEquals;

public final class ManualInspectionTest {

    @Test
    public void test() throws Exception {
        LocalFile dir = LocalFile.of(getExternalStorageDirectory()).resolve("test");
        dir.createDirs();
        try {
            dir.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        dir.resolve(".nomedia").createFiles();
        dir.resolve("html.html").createFiles();
        dir.resolve("zip.zip").createFiles();
        createNonUtf8Dir();

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
                "test.m4a");

        for (String res : resources) {
            File file = dir.resolve(res);
            if (file.exists(NOFOLLOW)) {
                continue;
            }

            Closer closer = Closer.create();
            try {
                InputStream in = closer.register(getInstrumentation().getContext().getAssets().open(res));
                file.copyFrom(in);
            } catch (Throwable e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }
        }

    }

    private void createNonUtf8Dir() throws IOException {

        byte[] nonUtf8 = {-19, -96, -67, -19, -80, -117};
        assertNotEquals(nonUtf8.clone(), new String(nonUtf8.clone(), UTF_8).getBytes(UTF_8));

        LocalFile dir = LocalFile.of(getExternalStorageDirectory()).resolve(nonUtf8, true);
        LocalFile child = dir.resolve("good we can see this dir");

        try {
            dir.deleteRecursive();
        } catch (FileNotFoundException ignored) {
        }

        dir.createDir();
        child.createFile();
    }

    private void createFutureFiles(File dir) throws IOException {
        dir.resolve("future")
                .createFiles()
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(365))
                );

        dir.resolve("future3")
                .createFiles()
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + DAYS.toMillis(2))
                );

        dir.resolve("future5")
                .createFiles()
                .setLastModifiedTime(
                        FOLLOW,
                        Instant.ofMillis(currentTimeMillis() + SECONDS.toMillis(5))
                );
    }

}
