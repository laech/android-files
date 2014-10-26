package l.files.fs.local;

import static l.files.fs.local.WatchEvent.Kind.CREATE;
import static l.files.fs.local.WatchEvent.Kind.DELETE;
import static l.files.fs.local.WatchEvent.Kind.MODIFY;

/**
 * Tests file system operations started with creating files/directories.
 *
 * @see android.os.FileObserver#CREATE
 */
public class WatchService_CREATE_InitiatedTest extends WatchServiceBaseTest {

  public void testCreateInEmptyDir_file() {
    testCreateInEmptyDir("a", FileType.FILE);
  }

  public void testCreateInEmptyDir_dir() {
    testCreateInEmptyDir("a", FileType.DIR);
  }

  private void testCreateInEmptyDir(String name, FileType type) {
    await(event(CREATE, name), newCreate(name, type));
  }

  public void testCreateInNonEmptyDir_file() {
    testCreateInNonEmptyDir(FileType.FILE);
  }

  public void testCreateInNonEmptyDir_dir() {
    testCreateInNonEmptyDir(FileType.DIR);
  }

  private void testCreateInNonEmptyDir(FileType type) {
    tmp().createFile("a");
    tmp().createDir("b");
    await(event(CREATE, "c"), newCreate("c", type));
  }

  public void testCreateDirThenCreateItemIntoIt_file() {
    testCreateDirThenCreateItemIntoIt(FileType.FILE);
  }

  public void testCreateDirThenCreateItemIntoIt_dir() {
    testCreateDirThenCreateItemIntoIt(FileType.DIR);
  }

  private void testCreateDirThenCreateItemIntoIt(FileType type) {
    await(event(CREATE, "a"), newCreate("a", FileType.DIR));
    await(event(MODIFY, "a"), newCreate("a/b", type));
  }

  public void testCreateDirThenDeleteItemFromIt_file() {
    testCreateDirThenDeleteItemFromIt(FileType.FILE);
  }

  public void testCreateDirThenDeleteItemFromIt_dir() {
    testCreateDirThenDeleteItemFromIt(FileType.DIR);
  }

  private void testCreateDirThenDeleteItemFromIt(FileType type) {
    await(event(CREATE, "a"), newCreate("a", FileType.DIR));
    await(event(MODIFY, "a"), newCreate("a/b", type));
    await(event(MODIFY, "a"), newDelete("a/b"));
  }

  public void testCreateDirThenMoveItemOutOfIt_file() {
    testCreateDirThenMoveItemOutOfIt(FileType.FILE);
  }

  public void testCreateDirThenMoveItemOutOfIt_dir() {
    testCreateDirThenMoveItemOutOfIt(FileType.DIR);
  }

  public void testCreateDirThenMoveItemOutOfIt(FileType type) {
    await(event(CREATE, "a"), newCreate("a", FileType.DIR));
    await(event(MODIFY, "a"), newCreate("a/b", type));
    await(event(MODIFY, "a"), newMoveFrom("a/b", helper().get("b")));
  }

  public void testCreateDirThenMoveFileIntoIt_file() {
    testCreateDirThenMoveItemIntoIt(FileType.FILE);
  }

  public void testCreateDirThenMoveFileIntoIt_dir() {
    testCreateDirThenMoveItemIntoIt(FileType.DIR);
  }

  public void testCreateDirThenMoveItemIntoIt(FileType type) {
    await(event(CREATE, "a"), newCreate("a", FileType.DIR));
    await(event(MODIFY, "a"), newMoveTo("a/b", type.create(helper().get("b"))));
  }

  public void testMultipleOperations() {
    await(event(CREATE, "a"), newCreate("a", FileType.DIR));
    await(event(CREATE, "b"), newCreate("b", FileType.DIR));
    await(event(MODIFY, "a"), newCreate("a/c", FileType.FILE));
    await(event(CREATE, "c"), newMoveTo("c", helper().createDir("c")));
    await(event(DELETE, "c"), newMoveFrom("c", helper().get("d")));
    await(event(DELETE, "b"), newDelete("b"));
    await(event(CREATE, "e"), newCreate("e", FileType.FILE));
  }
}
