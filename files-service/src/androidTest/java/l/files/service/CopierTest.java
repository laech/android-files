package l.files.service;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.service.Cancellables.NO_CANCEL;
import static l.files.service.Copier.Listener;

public final class CopierTest extends PasterTest {

  private static final Listener NULL_LISTENER = new Listener() {
    @Override
    public void onCopied(int remaining, long bytesCopied, long bytesTotal) {}
  };

  public void testCopiesDirectory() throws Exception {
    File srcDir = tempDir.newDirectory("a");
    File dstDir = tempDir.newDirectory("dst");
    File srcFile = new File(srcDir, "test.txt");
    File dstFile = new File(dstDir, "a/test.txt");
    write("Testing", srcFile, UTF_8);

    copy(srcDir, dstDir);
    assertEquals("Testing", Files.toString(srcFile, UTF_8));
    assertEquals("Testing", Files.toString(dstFile, UTF_8));
  }

  public void testCopiesFile() throws Exception {
    File srcFile = tempDir.newFile("test.txt");
    File dstDir = tempDir.newDirectory("dst");
    File dstFile = new File(dstDir, "test.txt");
    write("Testing", srcFile, UTF_8);

    copy(srcFile, dstDir);
    assertEquals("Testing", Files.toString(srcFile, UTF_8));
    assertEquals("Testing", Files.toString(dstFile, UTF_8));
  }

  private void copy(File src, File dstDir) throws IOException {
    create(NO_CANCEL, asList(src), dstDir).call();
  }

  @Override protected Copier create(
      Cancellable cancellable,
      Iterable<File> sources,
      File destination) {
    return create(cancellable, sources, destination, NULL_LISTENER, 0, 0);
  }

  private Copier create(
      Cancellable cancellable,
      Iterable<File> sources,
      File destination,
      Listener listener,
      int remaining,
      long length) {
    return new Copier(
        cancellable, sources, destination, listener, remaining, length);
  }
}
