package l.files.provider;

/**
 * Tests file system operations started with creating files/directories.
 *
 * @see android.os.FileObserver#CREATE
 */
public final class FilesProviderCreateInitiatedTest
    extends FilesProviderTestBase {

  public void testCreateFileToEmptyDir() {
    tester()
        .awaitCreateFile("a")
        .verify();
  }

  public void testCreateDirToEmptyDir() {
    tester()
        .awaitCreateDir("a")
        .verify();
  }

  public void testCreateFileToNonEmptyDir() {
    tmp().createFile("a");
    tmp().createDir("b");
    tester()
        .awaitCreateFile("c")
        .verify();
  }

  public void testCreateDirToNonEmptyDir() {
    tmp().createFile("a");
    tmp().createDir("b");
    tester()
        .awaitCreateDir("c")
        .verify();
  }

  /**
   * Directory added should be monitored for file additions to that directory,
   * as that will change the new directory's last modified date.
   */
  public void testCreateDirThenCreateFileIntoIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateFile("a/b")
        .verify();
  }

  /**
   * Directory added should be monitored for directory additions to that
   * directory, as that will change the new directory's last modified date.
   */
  public void testCreateDirThenCreateDirIntoIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateDir("a/b")
        .verify();
  }

  /**
   * Directory added should be monitored for file deletions from that directory,
   * as that will change the new directory's last modified date.
   */
  public void testCreateDirThenDeleteFileFromIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateFile("a/b")
        .awaitDelete("a/b")
        .verify();
  }

  /**
   * Directory added should be monitored for directory deletions from that
   * directory, as that will change the new directory's last modified date.
   */
  public void testCreateDirThenDeleteDirFromIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateDir("a/b")
        .awaitDelete("a/b")
        .verify();
  }

  /**
   * Directory added should be monitored for files additions into that
   * directory, as that will change the new directory's last modified date.
   */
  public void testCreateDirThenMoveFileOutOfIt() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateFile("a/b")
        .awaitMoveFrom("a/b", helper().get("b"))
        .verify();
  }

  /**
   * New directory added should be monitored for files moving into that
   * directory, as that will change the new directory's last modified date.
   */
  public void testCreateDirThenMoveFileIntoIt() {
    tester()
        .awaitCreateDir("a")
        .awaitMoveTo("a/b", helper().createDir("b"))
        .verify();
  }

  public void testMultipleOperations() {
    tester()
        .awaitCreateDir("a")
        .awaitCreateDir("b")
        .awaitCreateFile("a/c")
        .awaitMoveTo("c", helper().createDir("c"))
        .awaitMoveFrom("c", helper().get("d"))
        .awaitDelete("b")
        .awaitCreateFile("e")
        .verify();
  }
}
