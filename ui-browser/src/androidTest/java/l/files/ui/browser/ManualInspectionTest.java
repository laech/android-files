package l.files.ui.browser;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.testing.fs.Paths.createFiles;

public final class ManualInspectionTest {

    @Test
    public void test() throws Exception {
        Path dir = getExternalStorageDirectory().toPath().resolve("test");
        createDirectories(dir);
        try {
            setLastModifiedTime(dir, FileTime.fromMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        createFiles(dir.resolve(".nomedia"));
        createFiles(dir.resolve("html.html"));
        createFiles(dir.resolve("zip.zip"));

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
            "test.svg"
        );

        for (String res : resources) {
            Path file = dir.resolve(res);
            if (exists(file, NOFOLLOW_LINKS)) {
                continue;
            }

            try (InputStream in = getInstrumentation().getContext()
                .getAssets()
                .open(res)) {
                copy(in, file, REPLACE_EXISTING);
            }
        }

    }

    private void createFutureFiles(Path dir) throws IOException {
        setLastModifiedTime(
            createFiles(dir.resolve("future")),
            FileTime.fromMillis(currentTimeMillis() + DAYS.toMillis(365))
        );
        setLastModifiedTime(
            createFiles(dir.resolve("future3")),
            FileTime.fromMillis(currentTimeMillis() + DAYS.toMillis(2))
        );
        setLastModifiedTime(
            createFiles(dir.resolve("future5")),
            FileTime.fromMillis(currentTimeMillis() + SECONDS.toMillis(5))
        );
    }

}
