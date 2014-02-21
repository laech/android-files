package l.files.provider;

import static l.files.provider.FilesContract.getFileLocation;

/**
 * Tests file system operations started with deleting files/directories.
 */
public final class FilesProviderDeleteInitiatedTest
    extends FilesProviderTestBase {

  public void testDeleteFileNonEmptyDir() {
    tmp().createFile("a");
    tmp().createFile("b");
    tmp().createDir("c");
    tester()
        .awaitDelete("a")
        .verify();
  }

  public void testDeleteFileEmptyDir() {
    tmp().createFile("a");
    tester()
        .awaitDelete("a")
        .verify();
  }

  public void testDeleteDirNonEmptyDir() {
    tmp().createDir("a");
    tmp().createDir("b");
    tester()
        .awaitDelete("a")
        .verify();
  }

  public void testDeleteDirEmptyDir() {
    tmp().createDir("a");
    tester()
        .awaitDelete("a")
        .verify();
  }

  /**
   * Existing directory should be monitored after query for file deletions as
   * that will change its last modified date.
   */
  public void testDeleteFileFromExistingDirEmptyDir() {
    tmp().createFile("a/b");
    tester()
        .awaitDelete("a/b")
        .verify();
  }

  /**
   * Existing directory should be monitored after query for file deletions as
   * that will change its last modified date.
   */
  public void testDeleteFileFromExistingDirNonEmptyDir() {
    tmp().createFile("a/b");
    tmp().createFile("a/c");
    tester()
        .awaitDelete("a/b")
        .verify();
  }

  /**
   * Existing directory should be monitored after query for file deletions as
   * that will change its last modified date.
   */
  public void testDeleteDirFromExistingDirEmptyDir() {
    tmp().createDir("a/b");
    tester()
        .awaitDelete("a/b")
        .verify();
  }

  /**
   * Existing directory should be monitored after query for file deletions as
   * that will change its last modified date.
   */
  public void testDeleteDirFromExistingDirNonEmptyDir() {
    tmp().createDir("a/b");
    tmp().createDir("a/c");
    tester()
        .awaitDelete("a/b")
        .verify();
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
        .awaitDeleteRoot()
        .run(new Runnable() {
          @Override public void run() {
            tmp().createDir("a/b/c");
          }
        })
        .awaitCreateFile("a/b/c/d", "a/b/c")
        .verify("a/b/c");
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
        .awaitCreateFile("a/b")
        .verify();
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
        .awaitCreateFile("a/b")
        .verify();
  }

  public void testDeleteSelfThenCreateSelf() {
    tester()
        .monitor()
        .run(new Runnable() {
          @Override public void run() {
            tmp().delete();
            tmp().createRoot();
          }
        })
        .awaitCreateDir("a")
        .verify();
  }

  public void testDeleteSelfMoveDirWithSameNameIn() {
    tester()
        .monitor()
        .run(new Runnable() {
          @Override public void run() {
            tmp().delete();
            assertTrue(helper().createDir("a").renameTo(tmp().root()));
          }
        })
        .awaitCreateDir("b")
        .verify();
  }

  /**
   * Deleting the monitored root directory will cause any of its monitored
   * children to be stopped from being monitored.
   */
  public void testDeleteSelfChildrenWillNoLongerBeMonitored() {
    String location = getFileLocation(
        tester()
            .awaitCreateDir("a")
            .dir()
            .get("a")
    );

    tester().monitor("a");

    // TODO turn this into a provider method
    assertTrue(FilesDb.monitored.containsKey(location));
    assertTrue(FilesDb.observers.containsKey(location));

    tester().awaitDeleteRoot();

    assertFalse(FilesDb.monitored.containsKey(location));
    assertFalse(FilesDb.observers.containsKey(location));
  }

  /**
   * Deleting the monitored root directory will cause it to be stopped from
   * being monitored.
   */
  public void testDeleteSelfNoLongerMonitored() {
    String location = getFileLocation(
        tester()
            .awaitCreateDir("a")
            .dir()
            .root()
    );

    // TODO turn this into a provider method
    assertTrue(FilesDb.monitored.containsKey(location));
    assertTrue(FilesDb.observers.containsKey(location));

    tester().awaitDeleteRoot();

    assertFalse(FilesDb.monitored.containsKey(location));
    assertFalse(FilesDb.observers.containsKey(location));
  }
}
