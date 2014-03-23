package l.files.fse;

/**
 * Tests file system operations started with modifying files.
 *
 * @see android.os.FileObserver#MODIFY
 */
public final class FileEventService_MODIFY_InitiatedTest extends FileEventServiceBaseTest {

  public void testModifyFile() {
    tmp().createFile("a");
    tester().awaitModify("a");
  }
}
