package l.files.fse;

import l.files.io.Path;

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
    tester().monitor();
    Path path = Path.from(tmp().get());
    assertTrue(service().isMonitored(path));
    assertTrue(service().hasObserver(path));

    tester().awaitMoveRootTo(helper().get("b"));
    assertFalse(service().isMonitored(path));
    assertFalse(service().hasObserver(path));
  }

  /**
   * When the monitored directory is moved, stopping monitoring on its
   * children.
   */
  public void testMoveSelfNoLongerMonitorChildren() {
    tester().awaitCreateDir("a").monitor("a");
    Path path = Path.from(tmp().get("a"));
    assertTrue(service().isMonitored(path));
    assertTrue(service().hasObserver(path));

    tester().awaitMoveRootTo(helper().get("b"));
    assertFalse(service().isMonitored(path));
    assertFalse(service().hasObserver(path));
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
