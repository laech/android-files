package l.files.fse;

/**
 * Tests file system operations started with deleting files/directories.
 *
 * @see android.os.FileObserver#DELETE
 */
public class FileEventService_DELETE_InitiatedTest extends FileEventServiceBaseTest {

  public void testDeleteFileNonEmptyDir() {
    tmp().createFile("a");
    tmp().createFile("b");
    tmp().createDir("c");
    tester().awaitDelete("a");
  }

  public void testDeleteFileEmptyDir() {
    tmp().createFile("a");
    tester().awaitDelete("a");
  }

  public void testDeleteDirNonEmptyDir() {
    tmp().createDir("a");
    tmp().createDir("b");
    tester().awaitDelete("a");
  }

  public void testDeleteDirEmptyDir() {
    tmp().createDir("a");
    tester().awaitDelete("a");
  }

  /**
   * Existing directory should be monitored after query for file deletions as
   * that will change its last modified date.
   */
  public void testDeleteFileFromExistingDirEmptyDir() {
    tmp().createFile("a/b");
    tester().awaitDelete("a/b");
  }

  /**
   * Existing directory should be monitored after query for file deletions as
   * that will change its last modified date.
   */
  public void testDeleteFileFromExistingDirNonEmptyDir() {
    tmp().createFile("a/b");
    tmp().createFile("a/c");
    tester().awaitDelete("a/b");
  }

  /**
   * Existing directory should be monitored after query for file deletions as
   * that will change its last modified date.
   */
  public void testDeleteDirFromExistingDirEmptyDir() {
    tmp().createDir("a/b");
    tester().awaitDelete("a/b");
  }

  /**
   * Existing directory should be monitored after query for file deletions as
   * that will change its last modified date.
   */
  public void testDeleteDirFromExistingDirNonEmptyDir() {
    tmp().createDir("a/b");
    tmp().createDir("a/c");
    tester().awaitDelete("a/b");
  }

  /**
   * When a parent directory is monitored, and one of its child directory is
   * also monitored, delete the parent directory should stop monitoring on both
   * directories, recreating the child directory and start monitoring on it
   * should be of no problem.
   */
  public void testDeleteMonitoredParentAndMonitoredChildRecreateChild() {
    tmp().createDir("a/b/c");
    tester()
        .awaitCreateFile("a/b/c/d", "a/b/c")
        .awaitDeleteRoot();

    tmp().createDir("a/b/c");
    tester().awaitCreateFile("a/b/c/d", "a/b/c");
  }

  /**
   * When a monitored directory is deleted, then a new directory is added with
   * the same name, the new directory should be monitored.
   */
  public void testDeleteDirThenCreateDirWithSameName() {
    tmp().createDir("a");
    tester()
        .awaitDelete("a")
        .awaitCreateDir("a")
        .awaitDelete("a")
        .awaitCreateDir("a")
        .awaitCreateFile("a/b");
  }

  /**
   * When a monitored directory is deleted, then a new directory is moved in
   * with the same name, the new directory should be monitored.
   */
  public void testDeleteDirThenMoveDirInWithSameName() {
    tmp().createDir("a");
    tester()
        .awaitDelete("a")
        .awaitMoveTo("a", helper().createDir("a"))
        .awaitDelete("a")
        .awaitMoveTo("a", helper().createDir("b"))
        .awaitCreateFile("a/b");
  }
}
