package l.files.provider;

import static l.files.provider.FilesProviderTester.PermissionType;
import static l.files.provider.FilesProviderTester.PermissionType.READ;
import static l.files.provider.FilesProviderTester.PermissionType.WRITE;
import static l.files.provider.FilesProviderTester.FileType;
import static l.files.provider.FilesProviderTester.FileType.DIR;
import static l.files.provider.FilesProviderTester.FileType.FILE;

/**
 * Tests file system operations started with change files/directories
 * attributes.
 */
public final class FilesProviderAttribInitiatedTest
    extends FilesProviderTestBase {

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
        .awaitSetPermission(path, perm, true)
        .verify();
  }
}
