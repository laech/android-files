package l.files.fs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class MetaMagicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return MetaMagicDetector.INSTANCE;
    }

    @Test
    public void can_detect_by_name() throws Exception {
        Path file = createTextFile("a", "txt", "");
        assertEquals("text/plain", detector().detect(file));
    }

    @Test
    public void can_detect_by_content() throws Exception {
        Path file = createTextFile("a", "png");
        assertEquals("text/plain", detector().detect(file));
    }

}
