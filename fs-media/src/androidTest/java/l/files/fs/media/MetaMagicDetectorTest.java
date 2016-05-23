package l.files.fs.media;

import l.files.fs.Path;

public final class MetaMagicDetectorTest extends BasePropertyDetectorTest {

    @Override
    BasePropertyDetector detector() {
        return MetaMagicDetector.INSTANCE;
    }

    public void test_can_detect_by_name() throws Exception {
        Path file = createTextFile("a.txt", "");
        assertEquals("text/plain", detector().detect(getContext(), file));
    }

    public void test_can_detect_by_content() throws Exception {
        Path file = createTextFile("a.png");
        assertEquals("text/plain", detector().detect(getContext(), file));
    }

}
