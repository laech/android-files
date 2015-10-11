package l.files.fs.local;

import java.io.IOException;
import java.util.Collections;

import l.files.fs.File;
import l.files.fs.Permission;

public final class MetaMagicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return MetaMagicDetector.INSTANCE;
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
            detector().detect(LocalFile.create(file));
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

}
