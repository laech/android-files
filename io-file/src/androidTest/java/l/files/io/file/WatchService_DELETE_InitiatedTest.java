package l.files.io.file;

import static l.files.io.file.WatchEvent.Kind.CREATE;
import static l.files.io.file.WatchEvent.Kind.DELETE;
import static l.files.io.file.WatchEvent.Kind.MODIFY;

/**
 * Tests file system operations started with deleting files/directories.
 *
 * @see android.os.FileObserver#DELETE
 */
public class WatchService_DELETE_InitiatedTest extends WatchServiceBaseTest {

  public void testDeleteItemBecomeEmptyDir_file() {
    testDeleteItemBecomeEmptyDir(FileType.FILE);
  }

  public void testDeleteItemBecomeEmptyDir_dir() {
    testDeleteItemBecomeEmptyDir(FileType.DIR);
  }

  private void testDeleteItemBecomeEmptyDir(FileType type) {
    type.create(tmp().get("a"));
    await(event(DELETE, "a"), newDelete("a"));
  }

  public void testDeleteItemBecomeNonEmptyDir_file() {
    testDeleteItemBecomeNonEmptyDir(FileType.FILE);
  }

  public void testDeleteItemBecomeNonEmptyDir_dir() {
    testDeleteItemBecomeNonEmptyDir(FileType.DIR);
  }

  public void testDeleteItemBecomeNonEmptyDir(FileType type) {
    type.create(tmp().get("a"));
    tmp().createDir("b");
    tmp().createFile("c");
    await(event(DELETE, "a"), newDelete("a"));
  }

  public void testDeleteItemFromExistingDirEmptyDir_file() {
    testDeleteItemFromExistingDirEmptyDir(FileType.FILE);
  }

  public void testDeleteItemFromExistingDirEmptyDir_dir() {
    testDeleteItemFromExistingDirEmptyDir(FileType.DIR);
  }

  private void testDeleteItemFromExistingDirEmptyDir(FileType type) {
    type.create(tmp().get("a/b"));
    await(event(MODIFY, "a"), newDelete("a/b"));
  }

  public void testDeleteItemFromExistingDirNonEmptyDir_file() {
    testDeleteItemFromExistingDirNonEmptyDir(FileType.FILE);
  }

  public void testDeleteItemFromExistingDirNonEmptyDir_dir() {
    testDeleteItemFromExistingDirNonEmptyDir(FileType.DIR);
  }

  private void testDeleteItemFromExistingDirNonEmptyDir(FileType type) {
    type.create(tmp().get("a/b"));
    tmp().createFile("a/c");
    tmp().createDir("a/d");
    await(event(MODIFY, "a"), newDelete("a/b"));
  }

  /**
   * When a parent directory is monitored, and one of its child directory is
   * also monitored, delete the parent directory should stop monitoring on both
   * directories, recreating the child directory and start monitoring on it
   * should be of no problem.
   */
  public void testDeleteMonitoredParentAndMonitoredChildRecreateChild() {
    String parent = "a/b/c";
    String child = "a/b/c/d";
    tmp().createDir(parent);

    await(event(CREATE, child), newCreate(child, FileType.FILE), listen(parent));
    await(event(DELETE, tmpDir()), newDelete(tmpDir()));

    tmp().createDir(parent);
    await(event(CREATE, child), newCreate(child, FileType.FILE), listen(parent));
  }

  /**
   * When a monitored directory is deleted, then a new directory is added with
   * the same name, the new directory should be monitored.
   */
  public void testDeleteDirThenCreateDirWithSameName() {
    tmp().createDir("a");
    for (int i = 0; i < 2; i++) {
      await(event(DELETE, "a"), newDelete("a"));
      await(event(CREATE, "a"), newCreate("a", FileType.DIR));
      await(event(MODIFY, "a"), newCreate("a/b", FileType.FILE));
    }
  }

  /**
   * When a monitored directory is deleted, then a new directory is moved in
   * with the same name, the new directory should be monitored.
   */
  public void testDeleteDirThenMoveDirInWithSameName() {
    tmp().createDir("a");
    await(event(DELETE, "a"), newDelete("a"));
    await(event(CREATE, "a"), newMoveTo("a", helper().createDir("a")));
    await(event(DELETE, "a"), newDelete("a"));
    await(event(CREATE, "a"), newMoveTo("a", helper().createDir("b")));
    await(event(MODIFY, "a"), newCreate("a/b", FileType.FILE));
  }
}
