package l.files.fs.local;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.EnumSet;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;
import l.files.testing.BaseTest;

import static l.files.fs.Files.exists;
import static l.files.fs.Files.setPermissions;
import static l.files.fs.Files.traverse;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;

public abstract class PathBaseTest extends BaseTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final TestName testName = new TestName();

    private LocalPath dir1;
    private LocalPath dir2;

    protected final LocalPath dir1() {
        if (dir1 == null) {
            try {
                dir1 = LocalPath.of(folder.newFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected final LocalPath dir2() {
        if (dir2 == null) {
            try {
                dir2 = LocalPath.of(folder.newFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir2;
    }

    @Override
    public void tearDown() throws Exception {
        delete(dir1);
        delete(dir2);
        super.tearDown();
    }

    private static void delete(Path path) throws IOException {
        if (path == null || !exists(path, NOFOLLOW)) {
            return;
        }
        traverse(
                path,
                NOFOLLOW,
                new TraversalCallback.Base<Path>() {

                    @Override
                    public Result onPreVisit(Path path) throws IOException {
                        try {
                            setPermissions(path, EnumSet.allOf(Permission.class));
                        } catch (IOException ignore) {
                        }
                        return CONTINUE;
                    }

                    @Override
                    public Result onPostVisit(Path path) throws IOException {
                        Files.delete(path);
                        return CONTINUE;
                    }

                });
    }

}
