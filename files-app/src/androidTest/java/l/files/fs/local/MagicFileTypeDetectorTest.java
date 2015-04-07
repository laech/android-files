package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.write;
import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.common.testing.Tests.assertExists;

public final class MagicFileTypeDetectorTest extends LocalFileTypeDetectorTest {

    @Override
    protected LocalFileTypeDetector detector() {
        return MagicFileTypeDetector.INSTANCE;
    }

    public void testDetect_returnsOctetStreamForUnreadable() throws Exception {
        File file = tmp().createFile("a.txt");
        write("hello world", file, UTF_8);
        assertTrue(file.setReadable(false));
        try {
            detector().detect(LocalPath.of(file).getResource());
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    public void testDetect_returnsOctetStreamForSpecialFile() throws Exception {
        File file = new File("/proc/1/maps");
        assertExists(file);
        try {
            detector().detect(LocalPath.of(file).getResource());
            fail();
        } catch (IOException e) {
            // Pass
        }
    }
}
