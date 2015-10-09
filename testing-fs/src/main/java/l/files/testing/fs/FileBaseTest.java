package l.files.testing.fs;

import java.io.IOException;

import l.files.fs.File;
import l.files.fs.Visitor;
import l.files.fs.local.LocalFile;
import l.files.testing.BaseTest;

import static java.io.File.createTempFile;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;

public abstract class FileBaseTest extends BaseTest {

    private File dir1;
    private File dir2;

    protected final File dir1() {
        if (dir1 == null) {
            dir1 = LocalFile.create(createTempDir());
        }
        return dir1;
    }

    protected final File dir2() {
        if (dir2 == null) {
            dir2 = LocalFile.create(createTempDir());
        }
        return dir2;
    }

    private java.io.File createTempDir() {
        try {
            java.io.File file = createTempFile("test", null);
            assertTrue(file.delete());
            assertTrue(file.mkdirs());
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        delete(dir1);
        delete(dir2);
        super.tearDown();
    }

    private static void delete(File file) throws IOException {
        if (file == null) {
            return;
        }
        file.traverse(NOFOLLOW, new Visitor.Base() {

            @Override
            public Result onPostVisit(File file) throws IOException {
                file.delete();
                return CONTINUE;
            }

        });
    }

}
