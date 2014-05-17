package l.files.io.file.operations;

import com.google.common.io.Files;

import java.io.File;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.io.file.Files.readlink;
import static l.files.io.file.Files.symlink;

public final class MoveTest extends PasteTest {

  public void testMovesSymlink() throws Exception {
    File target = tmp().createFile("target");
    File link = tmp().get("link");
    symlink(target.getPath(), link.getPath());

    move(link, tmp().createDir("moved"));

    String expected = target.getPath();
    String actual = readlink(tmp().get("moved/link").getPath());
    assertEquals(expected, actual);
  }

  public void testMovesFile() throws Exception {
    File srcFile = tmp().createFile("a.txt");
    File dstDir = tmp().createDir("dst");
    File dstFile = new File(dstDir, "a.txt");
    write("Test", srcFile, UTF_8);

    move(srcFile, dstDir);

    assertFalse(srcFile.exists());
    assertEquals("Test", Files.toString(dstFile, UTF_8));
  }

  public void testMovesDirectory() throws Exception {
    File srcDir = tmp().createDir("a");
    File dstDir = tmp().createDir("dst");
    File srcFile = new File(srcDir, "test.txt");
    File dstFile = new File(dstDir, "a/test.txt");
    write("Test", srcFile, UTF_8);

    move(srcDir, dstDir);

    assertFalse(srcDir.exists());
    assertEquals("Test", Files.toString(dstFile, UTF_8));
  }

  @Override protected Move create(Iterable<String> sources, String dstDir) {
    return new Move(sources, dstDir);
  }

  private void move(File src, File dst) {
    create(asList(src.getPath()), dst.getPath()).call();
  }
}
