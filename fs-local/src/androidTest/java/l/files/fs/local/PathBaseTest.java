package l.files.fs.local;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.TraversalCallback;

import static l.files.fs.Files.exists;
import static l.files.fs.Files.setPermissions;
import static l.files.fs.Files.traverse;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.TraversalCallback.Result.CONTINUE;

public abstract class PathBaseTest extends AndroidTestCase {

    private Path dir1;
    private Path dir2;

    private File createTempFolder() throws IOException {
        File dir = File.createTempFile(getClass().getSimpleName(), null);
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        return dir;
    }

    protected final Path dir1() {
        if (dir1 == null) {
            try {
                dir1 = Path.fromFile(createTempFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected final Path dir2() {
        if (dir2 == null) {
            try {
                dir2 = Path.fromFile(createTempFolder());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir2;
    }

    @Override
    protected void tearDown() throws Exception {
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
