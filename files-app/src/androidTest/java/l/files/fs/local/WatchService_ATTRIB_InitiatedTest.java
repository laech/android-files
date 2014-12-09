package l.files.fs.local;

import l.files.fs.Path;
import l.files.fs.WatchEvent;

import static l.files.fs.WatchEvent.Kind.MODIFY;
import static l.files.fs.WatchEvent.Listener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Tests file system operations started with change files/directories
 * attributes.
 *
 * @see android.os.FileObserver#ATTRIB
 */
public class WatchService_ATTRIB_InitiatedTest extends WatchServiceBaseTest {

  public void testSelfChange() {
    Listener listener = mock(Listener.class);
    Path path = LocalPath.of(tmpDir());
    service().register(path, listener);
    try {

      assertTrue(tmpDir().setReadable(!tmpDir().canRead()));
      assertTrue(tmpDir().setReadable(!tmpDir().canRead()));
      verify(listener, timeout(1000)).onEvent(WatchEvent.create(MODIFY, path));

    } finally {
      service().unregister(path, listener);
    }
  }

  public void testLastModifiedDateChange_file() {
    testLastModifiedDateChange(FileType.FILE);
  }

  public void testLastModifiedDateChange_dir() {
    testLastModifiedDateChange(FileType.DIR);
  }

  private void testLastModifiedDateChange(FileType type) {
    type.create(tmp().get("a"));
    await(event(MODIFY, "a"), newLastModified("a", 2000));
  }

  public void testFileReadabilityChange() {
    testAttrChange("fr", FileType.FILE, Permission.READ);
  }

  public void testDirReadabilityChange() {
    testAttrChange("dr", FileType.DIR, Permission.READ);
  }

  public void testFileWritabilityChange() {
    testAttrChange("fw", FileType.FILE, Permission.WRITE);
  }

  public void testDirWritabilityChange() {
    testAttrChange("dw", FileType.DIR, Permission.WRITE);
  }

  private void testAttrChange(String name, FileType type, Permission perm) {
    type.create(tmp().get(name));

    WatchEvent event = event(MODIFY, name);
    Runnable change = newPermission(name, perm, !perm.get(tmp().get(name)));
    await(event, change);
  }
}
