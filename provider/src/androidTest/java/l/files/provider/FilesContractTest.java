package l.files.provider;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;

import static com.google.common.truth.Truth.ASSERT;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.timeout;
import static l.files.provider.FilesContract.copy;
import static l.files.provider.FilesContract.delete;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.FilesContract.getNameSuggestion;
import static l.files.provider.FilesContract.move;
import static l.files.provider.FilesContract.rename;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public final class FilesContractTest extends FileBaseTest {

  public void testGetIdFromDirectoryReturnsSameValueBeforeAfterDeletion()
      throws Exception {
    File dir = tmp().createDir("dir");
    String before = getFileId(dir);
    {
      forceDelete(dir);
    }
    String after = getFileId(dir);
    ASSERT.that(before).is(after);
  }

  public void testGetIdFromFileReturnsUri() throws Exception {
    File a = tmp().createDir("a");
    File b = tmp().createFile("b");
    ASSERT.that(getFileId(a)).is("file://" + tmp().get("a").getPath());
    ASSERT.that(getFileId(b)).is("file://" + tmp().get("b").getPath());
    ASSERT.that(getFileId(new File("/"))).is("file:///");
    ASSERT.that(getFileId(new File("/c/b/../hello"))).is("file:///c/hello");
    ASSERT.that(getFileId(new File("/c/./hello"))).is("file:///c/hello");
  }

  public void testRenamesFile() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().get("b");
    rename(getContext(), getFileId(a), b.getName());
    assertThat(a).doesNotExist();
    assertThat(b).exists();
  }

  public void testRenameThrowsExceptionOnError() throws Exception {
    File a = tmp().createFile("a");
    assertThat(tmp().get().setWritable(false)).isTrue();
    try {
      rename(getContext(), getFileId(a), "b");
      failBecauseExceptionWasNotThrown(IOException.class);
    } catch (IOException e) {
      // Pass
    }
  }

  public void testMovesFile() throws Exception {
    final File srcFile = tmp().createFile("a");
    final File dstDir = tmp().createDir("dst");

    move(getContext(), asList(getFileId(srcFile)), getFileId(dstDir));

    timeout(1, SECONDS, new Runnable() {
      @Override public void run() {
        File file = new File(dstDir, srcFile.getName());
        assertThat(file).exists();
      }
    });
  }

  public void testDeletesFile() throws Exception {
    final File file = tmp().createFile("a");
    String id = getFileId(file);

    delete(getContext(), asList(id));

    timeout(1, SECONDS, new Runnable() {
      @Override public void run() {
        assertThat(file).doesNotExist();
      }
    });
  }

  public void testCopiesFile() throws Exception {
    tmp().createFile("a/b");
    String srcId = getFileId(tmp().get("a"));
    String dstId = getFileId(tmp().createDir("1"));
    copy(getContext(), asList(srcId), dstId);
    assertThat(tmp().get("a/b")).exists();
  }

  public void testGetNameSuggestion() throws Exception {
    File file = tmp().createFile("a");
    String id = getFileId(tmp().get());
    String name = getNameSuggestion(getContext(), id, file.getName());
    assertThat(name).isEqualTo("a 2");
  }
}
