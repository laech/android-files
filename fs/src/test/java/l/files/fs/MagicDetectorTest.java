package l.files.fs;

import org.junit.Test;

import java.io.IOException;

import static l.files.fs.Files.MEDIA_TYPE_OCTET_STREAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;

public final class MagicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return MagicDetector.INSTANCE;
    }

    @Test
    public void detects_unreadable_file_as_octet_stream() throws Exception {
        Path file = createTextFile("a", "txt");
        FileSystem fs = file.fileSystem();
        doThrow(IOException.class)
                .when(fs)
                .stat(eq(file), any(LinkOption.class));
        try {
            detector().detect(file);
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    @Test
    public void detects_content_only_not_file_name() throws Exception {
        Path file = createTextFile("a", "txt", "");
        assertEquals(MEDIA_TYPE_OCTET_STREAM, detector().detect(file));
    }

}
