package l.files.fse;

import java.io.File;

import l.files.io.Path;

import static l.files.fse.WatchEvent.Kind.CREATE;
import static l.files.fse.WatchEvent.Kind.DELETE;
import static l.files.fse.WatchServiceBaseTest.FileType.DIR;

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
    Path path = Path.from(tmp().get());
    assertTrue(service().isMonitored(path));
    assertTrue(service().hasObserver(path));

    awaitMoveRootTo(helper().get("b"));
    assertFalse(service().isMonitored(path));
    assertFalse(service().hasObserver(path));
  }

  /**
   * When the monitored directory is moved, stopping monitoring on its
   * children.
   */
  public void testMoveSelfNoLongerMonitorChildren() {
    await(event(CREATE, "a"), newCreate("a", DIR));
    listen("a");

    Path path = Path.from(tmp().get("a"));
    assertTrue(service().isMonitored(path));
    assertTrue(service().hasObserver(path));

    awaitMoveRootTo(helper().get("b"));
    assertFalse(service().isMonitored(path));
    assertFalse(service().hasObserver(path));
  }

  public void testMoveSelfOutAddDirWithSameName() {
    awaitMoveRootTo(helper().get("test"));
    assertTrue(tmp().get().mkdirs());
    await(event(CREATE, "a"), newCreate("a", DIR));
  }

  public void testMoveSelfOutMoveDirWithSameNameIn() {
    awaitMoveRootTo(helper().get("a"));
    assertTrue(helper().createDir("b").renameTo(tmp().get()));
    await(event(CREATE, "c"), newCreate("c", DIR));
  }

  private void awaitMoveRootTo(File dst) {
    await(event(DELETE, tmpDir()), newMove(tmpDir(), dst));
  }
}
