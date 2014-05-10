package l.files.io.file.operations;

import java.io.File;
import java.util.List;

import l.files.common.testing.FileBaseTest;

import static android.test.MoreAsserts.assertEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.io.file.operations.Cancellables.CANCELLED;
import static l.files.io.file.operations.Cancellables.NO_CANCEL;

public abstract class PasteTest extends FileBaseTest {

  /**
   * When pasting emptying directories, they should be created on the
   * destination, even if they are empty.
   */
  public void testPastesEmptyDirectories() throws Exception {
    File src = tmp().createDir("empty");
    File dstDir = tmp().createDir("dst");
    create(NO_CANCEL, asList(src), dstDir).call();
    assertTrue(new File(dstDir, src.getName()).exists());
  }

  /**
   * When pasting files into a directory with existing files with the same
   * names, the existing files should not be overridden, new files will be
   * pasted with new names.
   */
  public void testDoesNotOverrideExistingFile() throws Exception {
    List<File> sources = asList(
        tmp().createFile("a.txt"),
        tmp().createFile("b.mp4")
    );
    tmp().createFile("1/a.txt");
    tmp().createFile("1/b.mp4");
    File dstDir = new File(tmp().get(), "1");
    create(NO_CANCEL, sources, dstDir).call();
    assertTrue(new File(tmp().get(), "1/a.txt").exists());
    assertTrue(new File(tmp().get(), "1/b.mp4").exists());
    assertTrue(new File(tmp().get(), "1/a 2.txt").exists());
    assertTrue(new File(tmp().get(), "1/b 2.mp4").exists());
  }

  /**
   * When pasting directories into a destination with existing directories with
   * the same names, the existing directories should not be overridden, new
   * directories will be pasted with new names.
   */
  public void testDoesNotOverrideExistingDirectory() throws Exception {
    tmp().createFile("a/1.txt");
    tmp().createFile("a/b/2.txt");
    tmp().createFile("a/b/3.txt");
    tmp().createFile("b/a/1.txt");
    List<File> sources = asList(new File(tmp().get(), "a"));
    File dstDir = new File(tmp().get(), "b");
    create(NO_CANCEL, sources, dstDir).call();
    assertTrue(new File(tmp().get(), "b/a/1.txt").exists());
    assertTrue(new File(tmp().get(), "b/a 2/1.txt").exists());
    assertTrue(new File(tmp().get(), "b/a 2/b/2.txt").exists());
    assertTrue(new File(tmp().get(), "b/a 2/b/3.txt").exists());
  }

  public void testDoesNothingIfAlreadyCancelledOnExecution() throws Exception {
    List<File> sources = asList(
        tmp().createFile("a/1.txt"),
        tmp().createFile("a/2.txt")
    );
    File destination = tmp().createDir("b");
    create(CANCELLED, sources, destination).call();
    assertEmpty(asList(destination.list()));
  }

  /**
   * Copying a parent directory into its own sub directory is forbidden as it
   * will result in an infinite loop.
   */
  public void testErrorOnPastingSelfIntoSubDirectory() throws Exception {
    File parent = tmp().createDir("parent");
    File child = tmp().createDir("parent/child");
    try {
      create(NO_CANCEL, singleton(parent), child).call();
      fail();
    } catch (CannotPasteIntoSelfException pass) {
      // Pass
    }
  }

  protected abstract Paste<?> create(
      Cancellable cancellable, Iterable<File> sources, File destination);
}
