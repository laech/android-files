package l.files.io.file.operations;

import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;

import l.files.common.testing.FileBaseTest;

import static android.test.MoreAsserts.assertEmpty;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

public abstract class PasteTest extends FileBaseTest {

  /**
   * When pasting emptying directories, they should be created on the
   * destination, even if they are empty.
   */
  public void testPastesEmptyDirectories() throws Exception {
    String src = tmp().createDir("empty").getPath();
    String dstDir = tmp().createDir("dst").getPath();
    create(asList(src), dstDir).call();
    assertTrue(tmp().get("dst/empty").exists());
  }

  /**
   * When pasting files into a directory with existing files with the same
   * names, the existing files should not be overridden, new files will be
   * pasted with new names.
   */
  public void testDoesNotOverrideExistingFile() throws Exception {
    List<String> sources = asList(
        tmp().createFile("a.txt").getPath(),
        tmp().createFile("b.mp4").getPath()
    );
    tmp().createFile("1/a.txt");
    tmp().createFile("1/b.mp4");
    String dstDir = new File(tmp().get(), "1").getPath();

    create(sources, dstDir).call();

    assertTrue(tmp().get("1/a.txt").exists());
    assertTrue(tmp().get("1/b.mp4").exists());
    assertTrue(tmp().get("1/a 2.txt").exists());
    assertTrue(tmp().get("1/b 2.mp4").exists());
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
    List<String> sources = asList(tmp().get("a").getPath());
    String dstDir = tmp().get("b").getPath();

    create(sources, dstDir).call();

    assertTrue(tmp().get("b/a/1.txt").exists());
    assertTrue(tmp().get("b/a 2/1.txt").exists());
    assertTrue(tmp().get("b/a 2/b/2.txt").exists());
    assertTrue(tmp().get("b/a 2/b/3.txt").exists());
  }

  public void testDoesNothingIfAlreadyCancelledOnExecution() throws Exception {
    final List<String> sources = asList(
        tmp().createFile("a/1.txt").getPath(),
        tmp().createFile("a/2.txt").getPath()
    );
    final File dstDir = tmp().createDir("b");

    Thread thread = new Thread(new Runnable() {
      @Override public void run() {
        currentThread().interrupt();
        try {
          create(sources, dstDir.getPath()).call();
          fail();
        } catch (CancellationException e) {
          // Pass
        }
      }
    });
    thread.start();
    thread.join();
    assertEmpty(asList(dstDir.list()));
  }

  public void testErrorOnPastingSelfIntoSubDirectory() throws Exception {
    String parent = tmp().createDir("parent").getPath();
    String child = tmp().createDir("parent/child").getPath();
    try {
      create(singleton(parent), child).call();
      fail();
    } catch (CannotPasteIntoSelfException pass) {
      // Pass
    }
  }

  public void testErrorOnPastingIntoSelf() throws Exception {
    String dir = tmp().createDir("parent").getPath();
    try {
      create(singleton(dir), dir).call();
      fail();
    } catch (CannotPasteIntoSelfException pass) {
      // Pass
    }
  }

  protected abstract Paste create(Iterable<String> sources, String dstDir);
}
