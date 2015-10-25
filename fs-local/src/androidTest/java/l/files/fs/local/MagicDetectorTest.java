package l.files.fs.local;

import java.io.IOException;
import java.util.Collections;

import l.files.fs.File;
import l.files.fs.Permission;

import static l.files.fs.File.MEDIA_TYPE_OCTET_STREAM;

public final class MagicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return MagicDetector.INSTANCE;
    }

    public void test_detects_unreadable_file_as_octet_stream() throws Exception {
        File file = dir1().resolve("a.txt").createFile();
        file.appendUtf8("hello world");
        file.setPermissions(Collections.<Permission>emptySet());
        try {
            detector().detect(file);
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    public void test_detects_special_file_as_octet_stream() throws Exception {
        java.io.File file = new java.io.File("/proc/1/maps");
        assertTrue(file.exists());
        try {
            detector().detect(LocalFile.of(file));
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    public void test_detects_content_only_not_file_name() throws Exception {
        File file = dir1().resolve("a.txt").createFile();
        assertEquals(MEDIA_TYPE_OCTET_STREAM, detector().detect(file));
    }

}
