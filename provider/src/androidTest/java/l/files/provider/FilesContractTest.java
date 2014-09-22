package l.files.provider;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.assertExists;
import static l.files.common.testing.Tests.assertNotExists;
import static l.files.common.testing.Tests.timeout;
import static l.files.provider.FilesContract.copy;
import static l.files.provider.FilesContract.delete;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.FilesContract.getNameSuggestion;
import static l.files.provider.FilesContract.move;
import static l.files.provider.FilesContract.rename;
import static org.apache.commons.io.FileUtils.forceDelete;

public final class FilesContractTest extends FileBaseTest {

  public void testGetIdFromDirectoryReturnsSameValueBeforeAfterDeletion()
      throws Exception {
    File dir = tmp().createDir("dir");
    String before = getFileId(dir);
    {
      forceDelete(dir);
    }
    String after = getFileId(dir);
    assertEquals(before, after);
  }

  public void testGetIdFromFileReturnsUri() throws Exception {
    File a = tmp().createDir("a");
    File b = tmp().createFile("b");
    assertEquals("file://" + tmp().get("a").getPath(), getFileId(a));
    assertEquals("file://" + tmp().get("b").getPath(), getFileId(b));
    assertEquals("file:///", getFileId(new File("/")));
    assertEquals("file:///c/hello", getFileId(new File("/c/b/../hello")));
    assertEquals("file:///c/hello", getFileId(new File("/c/./hello")));
  }

  public void testRenamesFile() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().get("b");
    rename(getContext(), getFileId(a), b.getName());
    assertNotExists(a);
    assertExists(b);
  }

  public void testRenameThrowsExceptionOnError() throws Exception {
    File a = tmp().createFile("a");
    assertTrue(tmp().get().setWritable(false));
    try {
      rename(getContext(), getFileId(a), "b");
      fail("Expecting " + IOException.class.getName());
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
        assertExists(file);
      }
    });
  }

  public void testDeletesFile() throws Exception {
    final File file = tmp().createFile("a");
    String id = getFileId(file);

    delete(getContext(), asList(id));

    timeout(1, SECONDS, new Runnable() {
      @Override public void run() {
        assertNotExists(file);
      }
    });
  }

  public void testCopiesFile() throws Exception {
    tmp().createFile("a/b");
    String srcId = getFileId(tmp().get("a"));
    String dstId = getFileId(tmp().createDir("1"));
    copy(getContext(), asList(srcId), dstId);
    assertExists(tmp().get("a/b"));
  }

  public void testGetNameSuggestion() throws Exception {
    File file = tmp().createFile("a");
    String id = getFileId(tmp().get());
    String name = getNameSuggestion(getContext(), id, file.getName());
    assertEquals("a 2", name);
  }
}
