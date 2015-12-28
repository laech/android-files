package l.files.testing.fs;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.testing.BaseTest;

public abstract class PathBaseTest extends BaseTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    Path dir1;
    Path dir2;

    protected Path dir1() {
        if (dir1 == null) {
            try {
                dir1 = Paths.get(folder.newFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected Path dir2() {
        if (dir2 == null) {
            try {
                dir2 = Paths.get(folder.newFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir2;
    }

}
