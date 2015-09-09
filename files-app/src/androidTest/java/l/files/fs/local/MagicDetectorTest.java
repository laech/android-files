package l.files.fs.local;

import java.io.IOException;
import java.util.Collections;

import l.files.fs.File;
import l.files.fs.Permission;

import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.common.testing.Tests.assertExists;
import static l.files.fs.File.OCTET_STREAM;

public final class MagicDetectorTest extends AbstractDetectorTest {

    @Override
    AbstractDetector detector() {
        return MagicDetector.INSTANCE;
    }

    public void test_detects_unreadable_file_as_octet_stream() throws Exception {
        File file = dir1().resolve("a.txt").createFile();
        file.writeString(UTF_8, "hello world");
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
        assertExists(file);
        try {
            detector().detect(LocalFile.create(file));
            fail();
        } catch (IOException e) {
            // Pass
        }
    }

    public void test_detects_content_only_not_file_name() throws Exception {
        File file = dir1().resolve("a.txt").createFile();
        assertEquals(OCTET_STREAM, detector().detect(file));
    }
}
