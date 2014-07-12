package l.files.io.file.operations;

import com.google.common.io.Files;

import java.io.File;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.io.file.Files.readlink;
import static l.files.io.file.Files.symlink;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class MoveTest extends PasteTest {

  public void testMovedCountInitialZero() {
    Move move = create(tmp().createFile("a"), tmp().createDir("b"));
    assertThat(move.getMovedItemCount(), is(0));
  }

  public void testMovesSymlink() throws Exception {
    File target = tmp().createFile("target");
    File link = tmp().get("link");
    symlink(target.getPath(), link.getPath());

    Move move = create(link, tmp().createDir("moved"));
    move.call();

    String expected = target.getPath();
    String actual = readlink(tmp().get("moved/link").getPath());
    assertThat(actual, is(expected));
    assertThat(move.getMovedItemCount(), is(1));
  }

  public void testMovesFile() throws Exception {
    File srcFile = tmp().createFile("a.txt");
    File dstDir = tmp().createDir("dst");
    File dstFile = new File(dstDir, "a.txt");
    write("Test", srcFile, UTF_8);

    Move move = create(srcFile, dstDir);
    move.call();

    assertFalse(srcFile.exists());
    assertThat(Files.toString(dstFile, UTF_8), is("Test"));
    assertThat(move.getMovedItemCount(), is(1));
  }

  public void testMovesDirectory() throws Exception {
    File srcDir = tmp().createDir("a");
    File dstDir = tmp().createDir("dst");
    File srcFile = new File(srcDir, "test.txt");
    File dstFile = new File(dstDir, "a/test.txt");
    write("Test", srcFile, UTF_8);

    Move move = create(srcDir, dstDir);
    move.call();

    assertFalse(srcDir.exists());
    assertThat(Files.toString(dstFile, UTF_8), is("Test"));
    assertThat(move.getMovedItemCount(), is(1));
  }

  @Override protected Move create(Iterable<String> sources, String dstDir) {
    return new Move(sources, dstDir);
  }

  private Move create(File src, File dst) {
    return create(asList(src.getPath()), dst.getPath());
  }
}
