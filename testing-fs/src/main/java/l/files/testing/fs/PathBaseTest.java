package l.files.testing.fs;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.TraversalCallback;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.TraversalCallback.Result.CONTINUE;

public abstract class PathBaseTest extends AndroidTestCase {

    @Nullable
    private ExtendedPath dir1;

    @Nullable
    private ExtendedPath dir2;

    @Override
    protected void tearDown() throws Exception {
        if (dir1 != null) {
            delete(dir1);
        }
        if (dir2 != null) {
            delete(dir2);
        }
        super.tearDown();
    }

    private void delete(Path path) throws IOException {
        if (!path.exists(NOFOLLOW)) {
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

    protected ExtendedPath dir1() {
        if (dir1 == null) {
            try {
                dir1 = ExtendedPath.wrap(Path.create(createTempFolder()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir1;
    }

    protected ExtendedPath dir2() {
        if (dir2 == null) {
            try {
                dir2 = ExtendedPath.wrap(Path.create(createTempFolder()));
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
