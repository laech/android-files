package l.files.fs.local;

import static l.files.fs.WatchEvent.Kind.MODIFY;

/**
 * Tests file system operations started with modifying files.
 *
 * @see android.os.FileObserver#MODIFY
 */
public class WatchService_MODIFY_InitiatedTest extends WatchServiceBaseTest {

  public void testModifyFile() {
    tmp().createFile("a");
    await(event(MODIFY, "a"), newModify("a"));
  }
}
