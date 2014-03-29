package l.files.fse;


/**
 * Tests file system operations started with moving files/directories from the
 * monitored directory.
 *
 * @see android.os.FileObserver#MOVED_FROM
 */
public class FileEventService_MOVE_FROM_InitiatedTest extends FileEventServiceBaseTest {

  public void testMoveFileOut() throws Exception {
    tmp().createFile("a");
    tmp().createFile("b");
    tester().awaitMoveFrom("a", helper().get("a"));
  }
}
