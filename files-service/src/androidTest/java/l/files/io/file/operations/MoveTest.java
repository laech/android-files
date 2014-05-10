package l.files.io.file.operations;

import com.google.common.io.Files;

import java.io.File;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.io.file.operations.Cancellables.NO_CANCEL;

public final class MoveTest extends PasteTest {

  public void testMovesFile() throws Exception {
    File srcFile = tmp().createFile("a.txt");
    File dstDir = tmp().createDir("dst");
    File dstFile = new File(dstDir, "a.txt");
    write("Test", srcFile, UTF_8);

    create(NO_CANCEL, asList(srcFile), dstDir).call();

    assertFalse(srcFile.exists());
    assertEquals("Test", Files.toString(dstFile, UTF_8));
  }

  public void testMovesDirectory() throws Exception {
    File srcDir = tmp().createDir("a");
    File dstDir = tmp().createDir("dst");
    File srcFile = new File(srcDir, "test.txt");
    File dstFile = new File(dstDir, "a/test.txt");
    write("Test", srcFile, UTF_8);

    create(NO_CANCEL, asList(srcDir), dstDir).call();

    assertFalse(srcDir.exists());
    assertEquals("Test", Files.toString(dstFile, UTF_8));
  }

  @Override
  protected Move create(
      Cancellable cancellable,
      Iterable<File> sources,
      File destination) {
    return new Move(cancellable, sources, destination);
  }
}
