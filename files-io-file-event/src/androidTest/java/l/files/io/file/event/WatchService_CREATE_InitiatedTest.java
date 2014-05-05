package l.files.io.file.event;

import static l.files.io.file.event.WatchEvent.Kind.CREATE;
import static l.files.io.file.event.WatchEvent.Kind.DELETE;
import static l.files.io.file.event.WatchEvent.Kind.MODIFY;
import static l.files.io.file.event.WatchServiceBaseTest.FileType.*;

/**
 * Tests file system operations started with creating files/directories.
 *
 * @see android.os.FileObserver#CREATE
 */
public class WatchService_CREATE_InitiatedTest extends WatchServiceBaseTest {

  public void testCreateInEmptyDir_file() {
    testCreateInEmptyDir("a", FILE);
  }

  public void testCreateInEmptyDir_dir() {
    testCreateInEmptyDir("a", DIR);
  }

  private void testCreateInEmptyDir(String name, FileType type) {
    await(event(CREATE, name), newCreate(name, type));
  }

  public void testCreateInNonEmptyDir_file() {
    testCreateInNonEmptyDir(FILE);
  }

  public void testCreateInNonEmptyDir_dir() {
    testCreateInNonEmptyDir(DIR);
  }

  private void testCreateInNonEmptyDir(FileType type) {
    tmp().createFile("a");
    tmp().createDir("b");
    await(event(CREATE, "c"), newCreate("c", type));
  }

  public void testCreateDirThenCreateItemIntoIt_file() {
    testCreateDirThenCreateItemIntoIt(FILE);
  }

  public void testCreateDirThenCreateItemIntoIt_dir() {
    testCreateDirThenCreateItemIntoIt(DIR);
  }

  private void testCreateDirThenCreateItemIntoIt(FileType type) {
    await(event(CREATE, "a"), newCreate("a", DIR));
    await(event(MODIFY, "a"), newCreate("a/b", type));
  }

  public void testCreateDirThenDeleteItemFromIt_file() {
    testCreateDirThenDeleteItemFromIt(FILE);
  }

  public void testCreateDirThenDeleteItemFromIt_dir() {
    testCreateDirThenDeleteItemFromIt(DIR);
  }

  private void testCreateDirThenDeleteItemFromIt(FileType type) {
    await(event(CREATE, "a"), newCreate("a", DIR));
    await(event(MODIFY, "a"), newCreate("a/b", type));
    await(event(MODIFY, "a"), newDelete("a/b"));
  }

  public void testCreateDirThenMoveItemOutOfIt_file() {
    testCreateDirThenMoveItemOutOfIt(FILE);
  }

  public void testCreateDirThenMoveItemOutOfIt_dir() {
    testCreateDirThenMoveItemOutOfIt(DIR);
  }

  public void testCreateDirThenMoveItemOutOfIt(FileType type) {
    await(event(CREATE, "a"), newCreate("a", DIR));
    await(event(MODIFY, "a"), newCreate("a/b", type));
    await(event(MODIFY, "a"), newMoveFrom("a/b", helper().get("b")));
  }

  public void testCreateDirThenMoveFileIntoIt_file() {
    testCreateDirThenMoveItemIntoIt(FILE);
  }

  public void testCreateDirThenMoveFileIntoIt_dir() {
    testCreateDirThenMoveItemIntoIt(DIR);
  }

  public void testCreateDirThenMoveItemIntoIt(FileType type) {
    await(event(CREATE, "a"), newCreate("a", DIR));
    await(event(MODIFY, "a"), newMoveTo("a/b", type.create(helper().get("b"))));
  }

  public void testMultipleOperations() {
    await(event(CREATE, "a"), newCreate("a", DIR));
    await(event(CREATE, "b"), newCreate("b", DIR));
    await(event(MODIFY, "a"), newCreate("a/c", FILE));
    await(event(CREATE, "c"), newMoveTo("c", helper().createDir("c")));
    await(event(DELETE, "c"), newMoveFrom("c", helper().get("d")));
    await(event(DELETE, "b"), newDelete("b"));
    await(event(CREATE, "e"), newCreate("e", FILE));
  }
}
