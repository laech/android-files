package l.files.fs;

import java.io.File;
import java.io.IOException;

import l.files.fs.local.LocalResource;

import static com.google.common.io.Files.write;
import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.common.testing.Tests.assertExists;

public final class MagicDetectorTest extends AbstractDetectorTest {

    @Override
    protected AbstractDetector detector() {
        return MagicDetector.INSTANCE;
    }

    public void testDetect_returnsOctetStreamForUnreadable() throws Exception {
        File file = tmp().createFile("a.txt");
        write("hello world", file, UTF_8);
        assertTrue(file.setReadable(false));
        try {
            detector().detect(LocalResource.create(file));
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    public void testDetect_returnsOctetStreamForSpecialFile() throws Exception {
        File file = new File("/proc/1/maps");
        assertExists(file);
        try {
            detector().detect(LocalResource.create(file));
            fail();
        } catch (IOException e) {
            // Pass
        }
    }
}
