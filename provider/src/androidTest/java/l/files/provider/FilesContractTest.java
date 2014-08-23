package l.files.provider;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.timeout;
import static l.files.provider.FilesContract.copy;
import static l.files.provider.FilesContract.delete;
import static l.files.provider.FilesContract.getFileLocation;
import static l.files.provider.FilesContract.move;
import static l.files.provider.FilesContract.rename;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public final class FilesContractTest extends FileBaseTest {

  public void testRenamesFile() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().get("b");
    rename(getContext(), getFileLocation(a), b.getName());
    assertThat(a).doesNotExist();
    assertThat(b).exists();
  }

  public void testRenameThrowsExceptionOnError() throws Exception {
    File a = tmp().createFile("a");
    assertThat(tmp().get().setWritable(false)).isTrue();
    try {
      rename(getContext(), getFileLocation(a), "b");
      failBecauseExceptionWasNotThrown(IOException.class);
    } catch (IOException e) {
      // Pass
    }
  }

  public void testMovesFile() throws Exception {
    final File srcFile = tmp().createFile("a");
    final File dstDir = tmp().createDir("dst");

    move(getContext(), asList(getFileLocation(srcFile)), getFileLocation(dstDir));

    timeout(1, SECONDS, new Runnable() {
      @Override public void run() {
        File file = new File(dstDir, srcFile.getName());
        assertThat(file).exists();
      }
    });
  }

  public void testDeletesFile() throws Exception {
    final File file = tmp().createFile("a");
    String location = getFileLocation(file);

    delete(getContext(), asList(location));

    timeout(1, SECONDS, new Runnable() {
      @Override public void run() {
        assertThat(file).doesNotExist();
      }
    });
  }

  public void testCopiesFile() throws Exception {
    tmp().createFile("a/b");
    String srcLocation = getFileLocation(tmp().get("a"));
    String dstLocation = getFileLocation(tmp().createDir("1"));
    copy(getContext(), asList(srcLocation), dstLocation);
    assertThat(tmp().get("a/b")).exists();
  }
}
