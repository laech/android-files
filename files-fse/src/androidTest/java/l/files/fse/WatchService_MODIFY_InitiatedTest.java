package l.files.fse;

/**
 * Tests file system operations started with modifying files.
 *
 * @see android.os.FileObserver#MODIFY
 */
public class WatchService_MODIFY_InitiatedTest extends WatchServiceBaseTest {

  public void testModifyFile() {
    tmp().createFile("a");
    tester().awaitModify("a");
  }
}
