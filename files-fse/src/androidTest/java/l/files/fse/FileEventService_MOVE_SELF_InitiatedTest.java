package l.files.fse;

/**
 * Tests file system operations started with move the root directory out.
 *
 * @see android.os.FileObserver#MOVE_SELF
 */
public class FileEventService_MOVE_SELF_InitiatedTest
    extends FileEventServiceBaseTest {

  /**
   * When the monitored directory itself is moved, stopping monitoring on it.
   */
  public void testMoveSelfNoLongerMonitorSelf() {
    tester().monitor();
    assertTrue(service().isMonitored(tmp().get()));
    assertTrue(service().hasObserver(tmp().get()));

    tester().awaitMoveRootTo(helper().get("b"));
    assertFalse(service().isMonitored(tmp().get()));
    assertFalse(service().hasObserver(tmp().get()));
  }

  /**
   * When the monitored directory is moved, stopping monitoring on its
   * children.
   */
  public void testMoveSelfNoLongerMonitorChildren() {
    tester().awaitCreateDir("a").monitor("a");
    assertTrue(service().isMonitored(tmp().get("a")));
    assertTrue(service().hasObserver(tmp().get("a")));

    tester().awaitMoveRootTo(helper().get("b"));
    assertFalse(service().isMonitored(tmp().get("a")));
    assertFalse(service().hasObserver(tmp().get("a")));
  }

  public void testMoveSelfOutAddDirWithSameName() {
    tester().awaitMoveRootTo(helper().get("test"));
    assertTrue(tmp().get().mkdirs());
    tester().awaitCreateDir("a");
  }

  public void testMoveSelfOutMoveDirWithSameNameIn() {
    tester().awaitMoveRootTo(helper().get("a"));
    assertTrue(helper().createDir("b").renameTo(tmp().get()));
    tester().awaitCreateDir("c");
  }
}
