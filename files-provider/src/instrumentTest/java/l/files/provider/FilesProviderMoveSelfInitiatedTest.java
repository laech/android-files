package l.files.provider;

/**
 * Tests file system operations started with move the root directory out.
 *
 * @see android.os.FileObserver#MOVE_SELF
 */
public final class FilesProviderMoveSelfInitiatedTest
    extends FilesProviderTestBase {

  /**
   * Move the monitored root directory to somewhere else, then changes to the
   * moved directory should no longer be monitored.
   */
  public void testMoveSelfNoLongerMonitored() throws Exception {
    tester().awaitMoveRootTo(helper().get("a"));
    helper().createFile("a/b");
    awaitEmpty();
  }

  /**
   * Move the monitored root directory to somewhere else, then the child
   * directories of the root directory should no longer be monitored.
   */
  public void testMoveSelfChildDirectoriesNoLongerMonitored() throws Exception {
    tester()
        .awaitCreateDir("a")
        .awaitMoveRootTo(helper().get("test"));
    helper().createFile("test/a/c");
    helper().createFile("test/a/d");
    awaitEmpty();
  }

  public void testMoveSelfOutAddDirWithSameName() {
    tester()
        .awaitMoveRootTo(helper().get("test"));

    assertTrue(tmp().get().mkdirs());

    tester()
        .awaitCreateDir("a")
        .verify();
  }

  public void testMoveSelfOutMoveDirWithSameNameIn() {
    tester().awaitMoveRootTo(helper().get("a"));

    assertTrue(helper().createDir("b").renameTo(tmp().get()));

    tester().awaitCreateDir("c").verify();
  }
}
