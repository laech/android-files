package l.files.fse;

/**
 * Tests file system operations started with creating files/directories.
 *
 * @see android.os.FileObserver#CREATE
 */
public class WatchService_CREATE_InitiatedTest extends WatchServiceBaseTest {

  public void testCreateFileToEmptyDir() {
    tester().awaitCreateFile("a");
  }

  public void testCreateDirToEmptyDir() {
    tester().awaitCreateDir("a");
  }

  public void testCreateFileToNonEmptyDir() {
    tmp().createFile("a");
    tmp().createDir("b");
    tester().awaitCreateFile("c");
  }

  public void testCreateDirToNonEmptyDir() {
    tmp().createFile("a");
    tmp().createDir("b");
    tester().awaitCreateDir("c");
  }

  /**
   * Directory added should be monitored for file additions to that directory,
   * as that will change the new directory's last modified date.
   */
  public void testCreateDirThenCreateFileIntoIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateFile("a/b");
  }

  /**
   * Directory added should be monitored for directory additions to that
   * directory, as that will change the new directory's last modified date.
   */
  public void testCreateDirThenCreateDirIntoIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateDir("a/b");
  }

  /**
   * Directory added should be monitored for file deletions from that directory,
   * as that will change the new directory's last modified date.
   */
  public void testCreateDirThenDeleteFileFromIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateFile("a/b")
        .awaitDelete("a/b");
  }

  /**
   * Directory added should be monitored for directory deletions from that
   * directory, as that will change the new directory's last modified date.
   */
  public void testCreateDirThenDeleteDirFromIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateDir("a/b")
        .awaitDelete("a/b");
  }

  /**
   * Directory added should be monitored for files additions into that
   * directory, as that will change the new directory's last modified date.
   */
  public void testCreateDirThenMoveFileOutOfIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateFile("a/b")
        .awaitMoveFrom("a/b", helper().get("b"));
  }

  /**
   * New directory added should be monitored for files moving into that
   * directory, as that will change the new directory's last modified date.
   */
  public void testCreateDirThenMoveFileIntoIt() {
    tester()
        .awaitCreateDir("a")
        .awaitMoveTo("a/b", helper().createDir("b"));
  }

  public void testMultipleOperations() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateDir("b")
        .awaitCreateFile("a/c")
        .awaitMoveTo("c", helper().createDir("c"))
        .awaitMoveFrom("c", helper().get("d"))
        .awaitDelete("b")
        .awaitCreateFile("e");
  }
}
