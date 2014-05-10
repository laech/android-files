package l.files.io.file.operations;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.io.file.operations.Cancellables.NO_CANCEL;
import static l.files.io.file.operations.Copy.Listener;

public final class CopyTest extends PasteTest {

  private static final Listener NULL_LISTENER = new Listener() {
    @Override
    public void onCopied(int remaining, long bytesCopied, long bytesTotal) {}
  };

  public void testCopiesDirectory() throws Exception {
    File srcDir = tmp().createDir("a");
    File dstDir = tmp().createDir("dst");
    File srcFile = new File(srcDir, "test.txt");
    File dstFile = new File(dstDir, "a/test.txt");
    write("Testing", srcFile, UTF_8);

    copy(srcDir, dstDir);
    assertEquals("Testing", Files.toString(srcFile, UTF_8));
    assertEquals("Testing", Files.toString(dstFile, UTF_8));
  }

  public void testCopiesFile() throws Exception {
    File srcFile = tmp().createFile("test.txt");
    File dstDir = tmp().createDir("dst");
    File dstFile = new File(dstDir, "test.txt");
    write("Testing", srcFile, UTF_8);

    copy(srcFile, dstDir);
    assertEquals("Testing", Files.toString(srcFile, UTF_8));
    assertEquals("Testing", Files.toString(dstFile, UTF_8));
  }

  private void copy(File src, File dstDir) throws IOException {
    create(NO_CANCEL, asList(src), dstDir).call();
  }

  @Override protected Copy create(
      Cancellable cancellable,
      Iterable<File> sources,
      File destination) {
    return create(cancellable, sources, destination, NULL_LISTENER, 0, 0);
  }

  private Copy create(
      Cancellable cancellable,
      Iterable<File> sources,
      File destination,
      Listener listener,
      int remaining,
      long length) {
    return new Copy(
        cancellable, sources, destination, listener, remaining, length);
  }
}
