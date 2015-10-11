package l.files.fs.local;

import l.files.fs.File;

public final class MetaMagicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return MetaMagicDetector.INSTANCE;
    }

    public void test_can_detect_by_name() throws Exception {
        File file = dir1().resolve("a.txt").createFile();
        assertEquals("text/plain", detector().detect(file));
    }

    public void test_can_detect_by_content() throws Exception {
        File file = dir1().resolve("a.png").createFile();
        file.writeAllUtf8("hello");
        assertEquals("text/plain", detector().detect(file));
    }

}
