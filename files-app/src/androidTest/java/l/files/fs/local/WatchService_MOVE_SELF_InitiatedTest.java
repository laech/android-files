package l.files.fs.local;

import java.io.File;

import static l.files.fs.WatchEvent.Kind.CREATE;
import static l.files.fs.WatchEvent.Kind.DELETE;

/**
 * Tests file system operations started with move the root directory out.
 *
 * @see android.os.FileObserver#MOVE_SELF
 */
public class WatchService_MOVE_SELF_InitiatedTest extends WatchServiceBaseTest {

    public void testMoveSelfOutAddDirWithSameName() {
        awaitMoveRootTo(helper().get("test"));
        assertTrue(tmp().get().mkdirs());
        await(event(CREATE, "a"), newCreate("a", FileType.DIR));
    }

    public void testMoveSelfOutMoveDirWithSameNameIn() {
        awaitMoveRootTo(helper().get("a"));
        assertTrue(helper().createDir("b").renameTo(tmp().get()));
        await(event(CREATE, "c"), newCreate("c", FileType.DIR));
    }

    private void awaitMoveRootTo(File dst) {
        await(event(DELETE, tmpDir()), newMove(tmpDir(), dst));
    }
}
