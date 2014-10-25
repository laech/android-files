package l.files.io.file;

import static l.files.io.file.WatchEvent.Kind.CREATE;
import static l.files.io.file.WatchEvent.Kind.DELETE;

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
   * When the monitored directory itself is deleted, stopping monitoring on it.
   */
  public void testDeleteSelfNoLongerMonitorSelf() {
    listen(tmpDir());
    Path path = Path.from(tmp().get());
    assertTrue(service().isMonitored(path));
    assertTrue(service().hasObserver(path));

    awaitDeleteRoot();
    assertFalse(service().isMonitored(path));
    assertFalse(service().hasObserver(path));
  }

  /**
   * When the monitored directory is deleted, stopping monitoring on its
   * children.
   */
  public void testDeleteSelfNoLongerMonitorChildren() {
    tmp().createDir("a");
    listen(tmpDir());
    listen("a");
    Path path = Path.from(tmp().get("a"));
    assertTrue(service().isMonitored(path));
    assertTrue(service().hasObserver(path));

    awaitDeleteRoot();
    assertFalse(service().isMonitored(path));
    assertFalse(service().hasObserver(path));
  }

  private void awaitDeleteRoot() {
    await(event(DELETE, tmpDir()), newDelete(tmpDir()));
  }
}