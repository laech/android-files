package l.files.fse;

/**
 * Tests file system operations started with moving files/directories to the
 * monitored directory.
 *
 * @see android.os.FileObserver#MOVED_TO
 */
public class WatchService_MOVE_TO_InitiatedTest extends WatchServiceBaseTest {

  /**
   * Directory moved into the monitored directory  should be monitored for files
   * additions in that directory, as that will change the new directory's last
   * modified date.
   */
  public void testMoveDirInThenAddFileIntoIt() {
    tester()
        .awaitMoveTo("a", helper().createDir("a"))
        .awaitCreateFile("a/b");
  }

  /**
   * Directory moved into the monitored directory should be monitored for files
   * deletions in that directory, as that will change the new directory's last
   * modified date.
   */
  public void testMoveDirInThenDeleteFileFromIt() {
    tester()
        .awaitMoveTo("a", helper().createDir("a"))
        .awaitCreateFile("a/b")
        .awaitDelete("a/b");
  }

  /**
   * Directory moved into the monitored directory should be monitored for files
   * moving into that directory, as that will change the new directory's last
   * modified date.
   */
  public void testMoveDirInThenMoveFileIntoIt() {
    tester()
        .awaitMoveTo("a", helper().createDir("a"))
        .awaitMoveTo("a/b", helper().createFile("b"));
  }

  /**
   * Directory moved into the monitored directory should be monitored for files
   * moving out of the directory, as that will change the directory's last
   * modified date.
   */
  public void testMoveDirInThenMoveFileOutOfIt() {
    tester()
        .awaitMoveTo("a", helper().createDir("a"))
        .awaitCreateFile("a/b")
        .awaitMoveFrom("a/b", helper().get("b"));
  }

  public void testMoveFileIn() {
    tester().awaitMoveTo("a", helper().createFile("a"));
  }
}
