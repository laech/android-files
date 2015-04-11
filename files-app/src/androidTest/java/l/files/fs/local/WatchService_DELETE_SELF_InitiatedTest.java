package l.files.fs.local;

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

    private void awaitDeleteRoot() {
        await(event(DELETE, tmpDir()), newDelete(tmpDir()));
    }

}
