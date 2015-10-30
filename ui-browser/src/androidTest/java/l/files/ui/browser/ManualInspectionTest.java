package l.files.ui.browser;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.local.LocalFile;

import static android.os.Environment.getExternalStorageDirectory;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ManualInspectionTest {

    @Test
    public void test() throws Exception {
        File dir = LocalFile.of(getExternalStorageDirectory()).resolve("test");
        dir.createDirs();
        try {
            dir.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        dir.resolve(".nomedia").createFiles();
        dir.resolve("html.html").createFiles();
        dir.resolve("zip.zip").createFiles();

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
            try (InputStream in = getInstrumentation().getContext().getAssets().open(res)) {
                file.copyFrom(in);
            }
        }

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
