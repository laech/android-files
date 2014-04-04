package l.files.fse;

import static l.files.fse.EventServiceTester.FileType;
import static l.files.fse.EventServiceTester.FileType.DIR;
import static l.files.fse.EventServiceTester.FileType.FILE;
import static l.files.fse.EventServiceTester.PermissionType;
import static l.files.fse.EventServiceTester.PermissionType.READ;
import static l.files.fse.EventServiceTester.PermissionType.WRITE;

/**
 * Tests file system operations started with change files/directories
 * attributes.
 *
 * @see android.os.FileObserver#ATTRIB
 */
public class FileEventService_ATTRIB_InitiatedTest extends FileEventServiceBaseTest {

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
