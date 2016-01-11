package l.files.testing.fs;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Paths;

public abstract class PathBaseTest extends AndroidTestCase {

    private Path dir1;
    private Path dir2;

    protected Path dir1() {
        if (dir1 == null) {
            try {
                dir1 = Paths.get(createTempFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected Path dir2() {
        if (dir2 == null) {
            try {
                dir2 = Paths.get(createTempFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir2;
    }

    private File createTempFolder() throws IOException {
        File dir = File.createTempFile(getClass().getSimpleName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }

}
