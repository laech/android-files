package l.files.ui.preview;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static l.files.fs.Files.ISO_8859_1;
import static l.files.ui.preview.DecodeText.readDetectingCharset;

public final class DecodeTextTest extends PathBaseTest {

    public void test_readDetectingCharset_utf8() throws Exception {
        Path file = Files.createFile(dir1().resolve("a"));
        Files.writeUtf8(file, "你好");
        assertEquals("", readDetectingCharset(file, 0));
        assertEquals("你", readDetectingCharset(file, 1));
        assertEquals("你好", readDetectingCharset(file, 2));
        assertEquals("你好", readDetectingCharset(file, 3));
    }

    public void test_readDetectingCharset_iso88591() throws Exception {
        Path file = Files.createFile(dir1().resolve("a"));
        Files.write(file, "hello world", ISO_8859_1);
        assertEquals("", readDetectingCharset(file, 0));
        assertEquals("h", readDetectingCharset(file, 1));
        assertEquals("he", readDetectingCharset(file, 2));
        assertEquals("hel", readDetectingCharset(file, 3));
        assertEquals("hello world", readDetectingCharset(file, 100));
    }

}
