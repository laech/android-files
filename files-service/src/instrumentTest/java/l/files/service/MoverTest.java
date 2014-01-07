package l.files.service;

import com.google.common.io.Files;

import java.io.File;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;

public final class MoverTest extends PasterTest {

  public void testMovesFile() throws Exception {
    File srcFile = tempDir.newFile("a.txt");
    File dstDir = tempDir.newDirectory("dst");
    File dstFile = new File(dstDir, "a.txt");
    write("Test", srcFile, UTF_8);

    create(NO_CANCEL, asList(srcFile), dstDir).call();

    assertFalse(srcFile.exists());
    assertEquals("Test", Files.toString(dstFile, UTF_8));
  }

  public void testMovesDirectory() throws Exception {
    File srcDir = tempDir.newDirectory("a");
    File dstDir = tempDir.newDirectory("dst");
    File srcFile = new File(srcDir, "test.txt");
    File dstFile = new File(dstDir, "a/test.txt");
    write("Test", srcFile, UTF_8);

    create(NO_CANCEL, asList(srcDir), dstDir).call();

    assertFalse(srcDir.exists());
    assertEquals("Test", Files.toString(dstFile, UTF_8));
  }

  @Override
  protected Mover create(
      Cancellable cancellable,
      Iterable<File> sources,
      File destination) {
    return new Mover(cancellable, sources, destination);
  }
}
