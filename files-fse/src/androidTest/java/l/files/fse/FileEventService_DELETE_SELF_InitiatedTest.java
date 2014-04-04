package l.files.fse;

/**
 * Tests file system operations started with deleting files/directories.
 *
 * @see android.os.FileObserver#DELETE_SELF
 */
public class FileEventService_DELETE_SELF_InitiatedTest extends FileEventServiceBaseTest {

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
    assertTrue(service().isMonitored(tmp().get()));
    assertTrue(service().hasObserver(tmp().get()));

    tester().awaitDeleteRoot();
    assertFalse(service().isMonitored(tmp().get()));
    assertFalse(service().hasObserver(tmp().get()));
  }

  /**
   * When the monitored directory is deleted, stopping monitoring on its
   * children.
   */
  public void testDeleteSelfNoLongerMonitorChildren() {
    tmp().createDir("a");
    tester().monitor().monitor("a");
    assertTrue(service().isMonitored(tmp().get("a")));
    assertTrue(service().hasObserver(tmp().get("a")));

    tester().awaitDeleteRoot();
    assertFalse(service().isMonitored(tmp().get("a")));
    assertFalse(service().hasObserver(tmp().get("a")));
  }
}
