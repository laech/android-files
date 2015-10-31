package l.files.testing.fs;

import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.local.LocalFile;
import l.files.testing.BaseTest;

import static org.junit.Assert.assertTrue;

public abstract class FileBaseTest extends BaseTest {

    @Rule
    public final TestName testName = new TestName();

    private File dir1;
    private File dir2;

    protected final File dir1() {
        if (dir1 == null) {
            dir1 = LocalFile.of(createTempDir());
        }
        return dir1;
    }

    protected final File dir2() {
        if (dir2 == null) {
            dir2 = LocalFile.of(createTempDir());
        }
        return dir2;
    }

    private java.io.File createTempDir() {
        try {
            String name = testName.getMethodName();
            if (name.length() > 100) {
                name = name.substring(0, 100);
            }
            java.io.File file = java.io.File.createTempFile(name + "_", null);
            assertTrue(file.delete());
            assertTrue(file.mkdirs());
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tearDown() throws Exception {
        delete(dir1);
        delete(dir2);
        super.tearDown();
    }

    private static void delete(File file) throws IOException {
        if (file == null) {
            return;
        }
        file.deleteRecursive();
    }

}
