package l.files.provider;


/**
 * Tests file system operations started with moving files/directories from the
 * monitored directory.
 *
 * @see android.os.FileObserver#MOVED_FROM
 */
public final class FilesProviderMoveFromInitiatedTest
    extends FilesProviderTestBase {

  /**
   * Move a monitored directory out, then changes to the moved directory should
   * no longer be monitored.
   */
  public void testMoveDirOutNoLongerMonitored() throws InterruptedException {
    tester()
        .awaitCreateDir("a")
        .awaitMoveFrom("a", helper().get("abc"));
    helper().createFile("abc/d");
    awaitEmpty();
  }

  public void testMoveFileOut() throws Exception {
    tmp().createFile("a");
    tmp().createFile("b");
    tester()
        .awaitMoveFrom("a", helper().get("a"))
        .verify();
  }
}
