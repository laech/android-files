package l.files.service;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.BaseTest;
import l.files.common.testing.TempDir;

import static java.util.Arrays.asList;
import static l.files.service.Cancellables.NO_CANCEL;
import static l.files.service.Deleter.Listener;

public final class DeleterTest extends BaseTest {

  private static final Listener NULL_LISTENER = new Listener() {
    @Override public void onFileDeleted(int total, int remaining) {}
  };

  private TempDir tempDir;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tempDir = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    tempDir.delete();
    super.tearDown();
  }

  public void testDeletesFile() throws Exception {
    File file = tempDir.newFile("a");
    assertTrue(file.exists());

    delete(file);
    assertFalse(file.exists());
  }

  public void testDeletesNonEmptyDirectory() throws Exception {
    File dir = tempDir.newDirectory("a");
    File file = tempDir.newFile("a/child.txt");
    assertTrue(dir.exists());
    assertTrue(file.exists());

    delete(dir);
    assertFalse(file.exists());
    assertFalse(dir.exists());
  }

  public void testDeletesEmptyDirectory() throws Exception {
    File dir = tempDir.newDirectory();
    assertTrue(dir.exists());

    delete(dir);
    assertFalse(dir.exists());
  }

  private void delete(File file) throws IOException {
    create(NO_CANCEL, asList(file), NULL_LISTENER).call();
  }

  private Deleter create(
      Cancellable cancellable,
      Iterable<File> files,
      Listener listener) {
    return create(cancellable, files, listener, 0);
  }

  private Deleter create(
      Cancellable cancellable,
      Iterable<File> files,
      Listener listener,
      int remaining) {
    return new Deleter(cancellable, files, listener, remaining);
  }
}
