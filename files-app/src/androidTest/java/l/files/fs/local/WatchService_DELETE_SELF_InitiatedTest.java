package l.files.fs.local;

import l.files.fs.Resource;

import static l.files.fs.WatchEvent.Kind.CREATE;
import static l.files.fs.WatchEvent.Kind.DELETE;

/**
 * Tests file system operations started with deleting files/directories.
 *
 * @see android.os.FileObserver#DELETE_SELF
 */
public class WatchService_DELETE_SELF_InitiatedTest extends WatchServiceBaseTest {

    public void testDeleteSelfThenCreateSelf() {
        awaitDeleteRoot();
        tmp().createRoot();
        await(event(CREATE, "a"), newCreate("a", FileType.DIR));
    }

    public void testDeleteSelfMoveDirWithSameNameIn() {
        awaitDeleteRoot();
        newMove(helper().createDir("a"), tmpDir()).run();
        await(event(CREATE, "b"), newCreate("b", FileType.DIR));
    }

    /**
     * When the monitored directory itself is deleted, stopping monitoring on
     * it.
     */
    public void testDeleteSelfNoLongerMonitorSelf() {
        listen(tmpDir());
        Resource resource = LocalResource.create(tmp().get());
        assertTrue(service().isRegistered(resource));
        assertTrue(service().hasObserver(resource));

        awaitDeleteRoot();
        assertFalse(service().isRegistered(resource));
        assertFalse(service().hasObserver(resource));
    }

    /**
     * When the monitored directory is deleted, stopping monitoring on its
     * children.
     */
    public void testDeleteSelfNoLongerMonitorChildren() {
        tmp().createDir("a");
        listen(tmpDir());
        listen("a");
        Resource resource = LocalResource.create(tmp().get("a"));
        assertTrue(service().isRegistered(resource));
        assertTrue(service().hasObserver(resource));

        awaitDeleteRoot();
        assertFalse(service().isRegistered(resource));
        assertFalse(service().hasObserver(resource));
    }

    private void awaitDeleteRoot() {
        await(event(DELETE, tmpDir()), newDelete(tmpDir()));
    }

}
