package l.files.io.file.operations;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;

import static java.util.Arrays.asList;
import static l.files.io.file.operations.Cancellables.NO_CANCEL;
import static l.files.io.file.operations.Delete.Listener;

public final class DeleteTest extends FileBaseTest {

  private static final Listener NULL_LISTENER = new Listener() {
    @Override public void onFileDeleted(int total, int remaining) {}
  };

  public void testDeletesFile() throws Exception {
    File file = tmp().createFile("a");
    assertTrue(file.exists());

    delete(file);
    assertFalse(file.exists());
  }

  public void testDeletesNonEmptyDirectory() throws Exception {
    File dir = tmp().createDir("a");
    File file = tmp().createFile("a/child.txt");
    assertTrue(dir.exists());
    assertTrue(file.exists());

    delete(dir);
    assertFalse(file.exists());
    assertFalse(dir.exists());
  }

  public void testDeletesEmptyDirectory() throws Exception {
    File dir = tmp().createDir("a");
    assertTrue(dir.exists());

    delete(dir);
    assertFalse(dir.exists());
  }

  private void delete(File file) throws IOException {
    create(NO_CANCEL, asList(file), NULL_LISTENER).call();
  }

  private Delete create(
      Cancellable cancellable,
      Iterable<File> files,
      Listener listener) {
    return create(cancellable, files, listener, 0);
  }

  private Delete create(
      Cancellable cancellable,
      Iterable<File> files,
      Listener listener,
      int remaining) {
    return new Delete(cancellable, files, listener, remaining);
  }
}
