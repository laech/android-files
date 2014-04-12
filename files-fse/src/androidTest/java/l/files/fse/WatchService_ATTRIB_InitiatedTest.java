package l.files.fse;

import static l.files.fse.WatchServiceTester.FileType;
import static l.files.fse.WatchServiceTester.FileType.DIR;
import static l.files.fse.WatchServiceTester.FileType.FILE;
import static l.files.fse.WatchServiceTester.PermissionType;
import static l.files.fse.WatchServiceTester.PermissionType.READ;
import static l.files.fse.WatchServiceTester.PermissionType.WRITE;

/**
 * Tests file system operations started with change files/directories
 * attributes.
 *
 * @see android.os.FileObserver#ATTRIB
 */
public class WatchService_ATTRIB_InitiatedTest extends WatchServiceBaseTest {

  public void testSetFileLastModified() {
    tester().awaitSetFileLastModified("file", 1);
  }

  public void testSetDirLastModified() {
    tester().awaitSetDirLastModified("dir", 2);
  }

  public void testSetFileReadable() {
    testSetAttr("fr", FILE, READ);
  }

  public void testSetDirReadable() {
    testSetAttr("dr", DIR, READ);
  }

  public void testSetFileWritable() {
    testSetAttr("fw", FILE, WRITE);
  }

  public void testSetDirWritable() {
    testSetAttr("dw", DIR, WRITE);
  }

  private void testSetAttr(String path, FileType type, PermissionType perm) {
    type.create(tmp().get(path));
    tester()
        .awaitSetPermission(path, perm, false)
        .awaitSetPermission(path, perm, true);
  }
}
