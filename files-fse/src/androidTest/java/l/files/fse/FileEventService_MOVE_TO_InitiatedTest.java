package l.files.fse;

import l.files.common.logging.Logger;

/**
 * Tests file system operations started with moving files/directories to the
 * monitored directory.
 *
 * @see android.os.FileObserver#MOVED_TO
 */
public final class FileEventService_MOVE_TO_InitiatedTest extends FileEventServiceBaseTest {

  /**
   * Directory moved into the monitored directory  should be monitored for files
   * additions in that directory, as that will change the new directory's last
   * modified date.
   */
  public void testMoveDirInThenAddFileIntoIt() {
    Logger.setDebugTagPrefix("testMoveDirInThenAddFileIntoIt");
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
    Logger.setDebugTagPrefix("testMoveDirInThenDeleteFileFromIt");
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
    Logger.setDebugTagPrefix("testMoveDirInThenMoveFileIntoIt");
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
    Logger.setDebugTagPrefix("testMoveDirInThenMoveFileOutOfIt");
    tester()
        .awaitMoveTo("a", helper().createDir("a"))
        .awaitCreateFile("a/b")
        .awaitMoveFrom("a/b", helper().get("b"));
  }

  public void testMoveFileIn() {
    Logger.setDebugTagPrefix("testMoveFileIn");
    tester().awaitMoveTo("a", helper().createFile("a"));
  }
}
