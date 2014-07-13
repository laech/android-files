package l.files.io.file.event;

import static l.files.io.file.event.WatchEvent.Kind.MODIFY;
import static l.files.io.file.event.WatchServiceBaseTest.FileType.*;
import static l.files.io.file.event.WatchServiceBaseTest.Permission.*;

/**
 * Tests file system operations started with change files/directories
 * attributes.
 *
 * @see android.os.FileObserver#ATTRIB
 */
public class WatchService_ATTRIB_InitiatedTest extends WatchServiceBaseTest {

  public void testLastModifiedDateChange_file() {
    testLastModifiedDateChange(FILE);
  }

  public void testLastModifiedDateChange_dir() {
    testLastModifiedDateChange(DIR);
  }

  private void testLastModifiedDateChange(FileType type) {
    type.create(tmp().get("a"));
    await(event(MODIFY, "a"), newLastModified("a", 2000));
  }

  public void testFileReadabilityChange() {
    testAttrChange("fr", FILE, READ);
  }

  public void testDirReadabilityChange() {
    testAttrChange("dr", DIR, READ);
  }

  public void testFileWritabilityChange() {
    testAttrChange("fw", FILE, WRITE);
  }

  public void testDirWritabilityChange() {
    testAttrChange("dw", DIR, WRITE);
  }

  private void testAttrChange(String name, FileType type, Permission perm) {
    type.create(tmp().get(name));

    WatchEvent event = event(MODIFY, name);
    Runnable change = newPermission(name, perm, !perm.get(tmp().get(name)));
    await(event, change);
  }
}
