package l.files.ui.browser;

import l.files.fs.Path;
import l.files.testing.fs.Paths;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.Files.setLastModifiedTime;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.base.io.Charsets.UTF_8;
import static l.files.testing.fs.Paths.createFiles;
import static org.junit.Assert.assertNotEquals;

public final class ManualInspectionTest {

    @Test
    public void test() throws Exception {
        Path dir = Path.of(getExternalStorageDirectory()).concat("test");
        dir.createDirectories();
        try {
            dir.setLastModifiedTime(FileTime.fromMillis(currentTimeMillis()));
        } catch (IOException ignore) {
            // Older versions does not support changing mtime
        }
        createFiles(dir.toJavaPath().resolve(".nomedia"));
        createFiles(dir.toJavaPath().resolve("html.html"));
        createFiles(dir.toJavaPath().resolve("zip.zip"));
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
            "test.svg"
        );

        for (String res : resources) {
            Path file = dir.concat(res);
            if (file.exists(NOFOLLOW_LINKS)) {
                continue;
            }

            try (InputStream in = getInstrumentation().getContext()
                .getAssets()
                .open(res)) {
                Paths.copy(in, file);
            }
        }

    }

    private void createNonUtf8Dir() throws IOException {

        byte[] nonUtf8 = {-19, -96, -67, -19, -80, -117};
        assertNotEquals(
            nonUtf8.clone(),
            new String(nonUtf8.clone(), UTF_8).getBytes(UTF_8)
        );

        Path dir = Path.of(getExternalStorageDirectory()).concat(nonUtf8);
        Path child = dir.concat("good we can see this dir");

        try {
            Paths.deleteRecursive(dir);
        } catch (FileNotFoundException | NoSuchFileException ignored) {
        }

        dir.createDirectory();
        child.createFile();
    }

    private void createFutureFiles(Path dir) throws IOException {
        setLastModifiedTime(
            createFiles(dir.toJavaPath().resolve("future")),
            FileTime.fromMillis(currentTimeMillis() + DAYS.toMillis(365))
        );
        setLastModifiedTime(
            createFiles(dir.toJavaPath().resolve("future3")),
            FileTime.fromMillis(currentTimeMillis() + DAYS.toMillis(2))
        );
        setLastModifiedTime(
            createFiles(dir.toJavaPath().resolve("future5")),
            FileTime.fromMillis(currentTimeMillis() + SECONDS.toMillis(5))
        );
    }

}
