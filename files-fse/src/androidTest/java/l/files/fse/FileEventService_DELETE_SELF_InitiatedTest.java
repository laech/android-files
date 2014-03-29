package l.files.fse;

import android.os.SystemClock;

import l.files.common.app.SystemServices;
import l.files.common.logging.Logger;

/**
 * Tests file system operations started with deleting files/directories.
 *
 * @see android.os.FileObserver#DELETE_SELF
 */
public class FileEventService_DELETE_SELF_InitiatedTest extends FileEventServiceBaseTest {

  public void testDeleteSelfThenCreateSelf() {
    Logger.setDebugTagPrefix("testDeleteSelfThenCreateSelf");
    tester()
        .awaitDeleteRoot()
        .awaitCreateRoot()
        .awaitCreateDir("a");
  }

  public void testDeleteSelfMoveDirWithSameNameIn() {
    Logger.setDebugTagPrefix("testDeleteSelfMoveDirWithSameNameIn");
    tester()
        .awaitDeleteRoot()
        .awaitMoveToRoot(helper().createDir("a"))
        .awaitCreateDir("b");
  }

  /**
   * When the monitored directory itself is deleted, stopping monitoring on it.
   */
  public void testDeleteSelfNoLongerMonitorSelf() {
    Logger.setDebugTagPrefix("testDeleteSelfNoLongerMonitorSelf");
    tester().monitor();
    assertTrue(manager().isMonitored(tmp().get()));
    assertTrue(manager().hasObserver(tmp().get()));

    tester().awaitDeleteRoot();
    assertFalse(manager().isMonitored(tmp().get()));
    assertFalse(manager().hasObserver(tmp().get()));
  }

  /**
   * When the monitored directory is deleted, stopping monitoring on its
   * children.
   */
  public void testDeleteSelfNoLongerMonitorChildren() {
    tmp().createDir("a");
    tester().monitor().monitor("a");
    assertTrue(manager().isMonitored(tmp().get("a")));
    assertTrue(manager().hasObserver(tmp().get("a")));

    tester().awaitDeleteRoot();
    assertFalse(manager().isMonitored(tmp().get("a")));
    assertFalse(manager().hasObserver(tmp().get("a")));
  }

  public void testDeleteRecreate() {
    Logger.setDebugTagPrefix("testDeleteRecreate");
    tester().monitor(tmp().get().getParentFile());
    tmp().delete();
    tmp().createRoot();
    tmp().delete();
    tmp().createRoot();
    helper().delete();
    helper().createRoot();
    helper().delete();
    helper().createRoot();
    tester().awaitMoveRootTo(helper().get("a"));
    assertTrue(helper().createDir("b").renameTo(tmp().get()));
    tester().awaitCreateDir("c");
  }
}
