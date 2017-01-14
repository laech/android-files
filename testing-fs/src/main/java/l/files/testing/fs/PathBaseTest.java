package l.files.testing.fs;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import l.files.fs.FileSystem;
import l.files.fs.Path;

import static l.files.base.Objects.requireNonNull;

public abstract class PathBaseTest extends AndroidTestCase {

    protected final ExtendedFileSystem fs;

    @Nullable
    private Path dir1;

    @Nullable
    private Path dir2;

    public PathBaseTest(FileSystem fs) {
        this.fs = new ExtendedFileSystem(fs);
    }

    @Override
    protected void tearDown() throws Exception {
        if (dir1 != null) {
            fs.deleteRecursiveIfExists(dir1);
        }
        if (dir2 != null) {
            fs.deleteRecursiveIfExists(dir2);
        }
        super.tearDown();
    }

    protected Path dir1() {
        if (dir1 == null) {
            try {
                dir1 = Path.fromFile(createTempFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected Path dir2() {
        if (dir2 == null) {
            try {
                dir2 = Path.fromFile(createTempFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir2;
    }

    private File createTempFolder() throws IOException {
        File dir = File.createTempFile(getClass().getSimpleName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdirs());
        return dir;
    }

}
