package linux;

import java.io.File;
import java.io.IOException;

import static java.io.File.createTempFile;
import static junit.framework.Assert.assertTrue;

final class TempDir {

    private TempDir() {
    }

    static File createTempDir(String prefix) throws IOException {
        File dir = createTempFile(prefix, null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }
}
