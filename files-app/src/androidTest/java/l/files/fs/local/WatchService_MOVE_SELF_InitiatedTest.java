package l.files.fs.local;

import java.io.File;

import l.files.fs.Resource;

import static l.files.fs.WatchEvent.Kind.CREATE;
import static l.files.fs.WatchEvent.Kind.DELETE;

/**
 * Tests file system operations started with move the root directory out.
 *
 * @see android.os.FileObserver#MOVE_SELF
 */
public class WatchService_MOVE_SELF_InitiatedTest
        extends WatchServiceBaseTest {

    /**
     * When the monitored directory itself is moved, stopping monitoring on it.
     */
    public void testMoveSelfNoLongerMonitorSelf() {
        listen(tmpDir());
        Resource resource = LocalResource.create(tmp().get());
        assertTrue(service().isRegistered(resource));
        assertTrue(service().hasObserver(resource));

        awaitMoveRootTo(helper().get("b"));
        assertFalse(service().isRegistered(resource));
        assertFalse(service().hasObserver(resource));
    }

    /**
     * When the monitored directory is moved, stopping monitoring on its
     * children.
     */
    public void testMoveSelfNoLongerMonitorChildren() {
        await(event(CREATE, "a"), newCreate("a", FileType.DIR));
        listen("a");

        Resource resource = LocalResource.create(tmp().get("a"));
        assertTrue(service().isRegistered(resource));
        assertTrue(service().hasObserver(resource));

        awaitMoveRootTo(helper().get("b"));
        assertFalse(service().isRegistered(resource));
        assertFalse(service().hasObserver(resource));
    }

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
