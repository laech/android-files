package l.files.provider;


import android.database.Cursor;

/**
 * Tests file system operations started with moving files/directories from the
 * monitored directory.
 *
 * @see android.os.FileObserver#MOVED_FROM
 */
public final class FilesProviderMoveFromInitiatedTest
    extends FilesProviderTestBase {

  private static final int AWAIT_MILLIS = 500;

  /**
   * Move a monitored directory out, then changes to the moved directory should
   * no longer be monitored.
   */
  public void testMoveDirOutNoLongerMonitored() throws InterruptedException {
    tester()
        .awaitCreateDir("a")
        .awaitMoveFrom("a", helper().get("abc"));

    helper().createFile("abc/d");

    Thread.sleep(AWAIT_MILLIS);

    Cursor cursor = tester().query();
    //noinspection TryFinallyCanBeTryWithResources
    try {
      assertEquals(0, cursor.getCount());
    } finally {
      cursor.close();
    }
  }

  public void testMoveFileOut() throws Exception {
    tmp().createFile("a");
    tmp().createFile("b");
    tester()
        .awaitMoveFrom("a", helper().get("a"))
        .verify();
  }
}
