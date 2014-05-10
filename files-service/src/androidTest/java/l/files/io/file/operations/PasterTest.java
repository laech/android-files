package l.files.io.file.operations;

import java.io.File;
import java.util.List;

import l.files.common.testing.BaseTest;
import l.files.common.testing.TempDir;

import static android.test.MoreAsserts.assertEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.io.file.operations.Cancellables.CANCELLED;
import static l.files.io.file.operations.Cancellables.NO_CANCEL;

public abstract class PasterTest extends BaseTest {

  protected TempDir tempDir;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tempDir = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    tempDir.delete();
    super.tearDown();
  }

  /**
   * When pasting emptying directories, they should be created on the
   * destination, even if they are empty.
   */
  public void testPastesEmptyDirectories() throws Exception {
    File src = tempDir.newDirectory("empty");
    File dstDir = tempDir.newDirectory("dst");
    create(NO_CANCEL, asList(src), dstDir).call();
    assertTrue(new File(dstDir, src.getName()).exists());
  }

  /**
   * When pasting files into a directory with existing files with the same
   * names, the existing files should not be overridden, new files will be
   * pasted with new names.
   */
  public void testDoesNotOverrideExistingFile() throws Exception {
    List<File> sources = tempDir.newFiles("a.txt", "b.mp4");
    tempDir.newFiles("1/a.txt", "1/b.mp4");
    File dstDir = new File(tempDir.get(), "1");
    create(NO_CANCEL, sources, dstDir).call();
    assertTrue(new File(tempDir.get(), "1/a.txt").exists());
    assertTrue(new File(tempDir.get(), "1/b.mp4").exists());
    assertTrue(new File(tempDir.get(), "1/a 2.txt").exists());
    assertTrue(new File(tempDir.get(), "1/b 2.mp4").exists());
  }

  /**
   * When pasting directories into a destination with existing directories with
   * the same names, the existing directories should not be overridden, new
   * directories will be pasted with new names.
   */
  public void testDoesNotOverrideExistingDirectory() throws Exception {
    tempDir.newFiles("a/1.txt", "a/b/2.txt", "a/b/3.txt");
    tempDir.newFiles("b/a/1.txt");
    List<File> sources = asList(new File(tempDir.get(), "a"));
    File dstDir = new File(tempDir.get(), "b");
    create(NO_CANCEL, sources, dstDir).call();
    assertTrue(new File(tempDir.get(), "b/a/1.txt").exists());
    assertTrue(new File(tempDir.get(), "b/a 2/1.txt").exists());
    assertTrue(new File(tempDir.get(), "b/a 2/b/2.txt").exists());
    assertTrue(new File(tempDir.get(), "b/a 2/b/3.txt").exists());
  }

  public void testDoesNothingIfAlreadyCancelledOnExecution() throws Exception {
    List<File> sources = tempDir.newFiles("a/1.txt", "a/2.txt");
    File destination = tempDir.newDirectory("b");
    create(CANCELLED, sources, destination).call();
    assertEmpty(asList(destination.list()));
  }

  /**
   * Copying a parent directory into its own sub directory is forbidden as it
   * will result in an infinite loop.
   */
  public void testErrorOnPastingSelfIntoSubDirectory() throws Exception {
    File parent = tempDir.newDirectory("parent");
    File child = tempDir.newDirectory("parent/child");
    try {
      create(NO_CANCEL, singleton(parent), child).call();
      fail();
    } catch (CannotPasteIntoSelfException pass) {}
  }

  protected abstract Paster<?> create(
      Cancellable cancellable, Iterable<File> sources, File destination);
}
