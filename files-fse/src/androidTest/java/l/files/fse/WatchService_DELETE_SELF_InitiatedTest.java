package l.files.fse;

import l.files.io.Path;

/**
 * Tests file system operations started with deleting files/directories.
 *
 * @see android.os.FileObserver#DELETE_SELF
 */
public class WatchService_DELETE_SELF_InitiatedTest extends WatchServiceBaseTest {

  public void testDeleteSelfThenCreateSelf() {
    tester()
        .awaitDeleteRoot()
        .awaitCreateRoot()
        .awaitCreateDir("a");
  }

  public void testDeleteSelfMoveDirWithSameNameIn() {
    tester()
        .awaitDeleteRoot()
        .awaitMoveToRoot(helper().createDir("a"))
        .awaitCreateDir("b");
  }

  /**
   * When the monitored directory itself is deleted, stopping monitoring on it.
   */
  public void testDeleteSelfNoLongerMonitorSelf() {
    tester().monitor();
    Path path = Path.from(tmp().get());
    assertTrue(service().isMonitored(path));
    assertTrue(service().hasObserver(path));

    tester().awaitDeleteRoot();
    assertFalse(service().isMonitored(path));
    assertFalse(service().hasObserver(path));
  }

  /**
   * When the monitored directory is deleted, stopping monitoring on its
   * children.
   */
  public void testDeleteSelfNoLongerMonitorChildren() {
    tmp().createDir("a");
    tester().monitor().monitor("a");
    Path path = Path.from(tmp().get("a"));
    assertTrue(service().isMonitored(path));
    assertTrue(service().hasObserver(path));

    tester().awaitDeleteRoot();
    assertFalse(service().isMonitored(path));
    assertFalse(service().hasObserver(path));
  }
}
