package l.files.testing.fs;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import l.files.fs.local.LocalPath;
import l.files.testing.BaseTest;

public abstract class PathBaseTest extends BaseTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    LocalPath dir1;
    LocalPath dir2;

    protected LocalPath dir1() {
        if (dir1 == null) {
            try {
                dir1 = LocalPath.of(folder.newFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected LocalPath dir2() {
        if (dir2 == null) {
            try {
                dir2 = LocalPath.of(folder.newFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir2;
    }

}
