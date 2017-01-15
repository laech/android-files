package l.files.testing.fs;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import l.files.fs.Path;

public abstract class PathBaseTest extends AndroidTestCase {

    @Nullable
    private ExtendedPath dir1;

    @Nullable
    private ExtendedPath dir2;

    @Override
    protected void tearDown() throws Exception {
        if (dir1 != null) {
            dir1.deleteRecursiveIfExists();
        }
        if (dir2 != null) {
            dir2.deleteRecursiveIfExists();
        }
        super.tearDown();
    }

    protected abstract Path create(File file);

    protected Path dir1() {
        if (dir1 == null) {
            try {
                dir1 = new ExtendedPath(create(createTempFolder()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected Path dir2() {
        if (dir2 == null) {
            try {
                dir2 = new ExtendedPath(create(createTempFolder()));
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
