package l.files.testing.fs;

import androidx.annotation.Nullable;
import l.files.fs.Path;
import l.files.fs.TraversalCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.attribute.PosixFilePermission.*;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;
import static org.junit.Assert.assertTrue;

public abstract class PathBaseTest {

    @Rule
    public final TestName testName = new TestName();

    @Nullable
    private Path dir1;

    @Nullable
    private Path dir2;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        if (dir1 != null) {
            delete(dir1);
        }
        if (dir2 != null) {
            delete(dir2);
        }
    }

    private void delete(Path path) throws IOException {
        if (!path.exists(NOFOLLOW_LINKS)) {
            return;
        }
        path.traverse(NOFOLLOW, new TraversalCallback.Base<Path>() {

            @Override
            public Result onPreVisit(Path path) throws IOException {
                if (path.stat(NOFOLLOW).isDirectory()) {
                    path.setPermissions(EnumSet.of(
                        OWNER_READ, OWNER_WRITE, OWNER_EXECUTE));
                }
                return CONTINUE;
            }

            @Override
            public Result onPostVisit(Path element) throws IOException {
                element.delete();
                return CONTINUE;
            }
        });
    }

    protected Path dir1() {
        if (dir1 == null) {
            try {
                dir1 = Path.of(createTempFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected Path dir2() {
        if (dir2 == null) {
            try {
                dir2 = Path.of(createTempFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir2;
    }

    private File createTempFolder() throws IOException {
        File dir = File.createTempFile(getClass().getSimpleName()
            + "." + testName.getMethodName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdirs());
        return dir;
    }

}
