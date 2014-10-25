package l.files.io.file;

import java.io.File;

import static l.files.io.file.WatchEvent.Kind.CREATE;
import static l.files.io.file.WatchEvent.Kind.DELETE;

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
    await(event(CREATE, "a"), newCreate("a", FileType.DIR));
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