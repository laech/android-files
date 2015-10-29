package l.files.fs;

import org.junit.Test;

import java.io.IOException;

import static l.files.fs.File.MEDIA_TYPE_OCTET_STREAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

public final class MagicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return MagicDetector.INSTANCE;
    }

    @Test
    public void detects_unreadable_file_as_octet_stream() throws Exception {
        File file = createTextFile("a.txt");
        doThrow(IOException.class).when(file).stat(any(LinkOption.class));
        try {
            detector().detect(file);
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    @Test
    public void detects_content_only_not_file_name() throws Exception {
        File file = createTextFile("a.txt", "");
        assertEquals(MEDIA_TYPE_OCTET_STREAM, detector().detect(file));
    }

}
