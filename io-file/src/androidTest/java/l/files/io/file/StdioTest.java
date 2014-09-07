package l.files.io.file;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static l.files.io.file.Stdio.remove;
import static l.files.io.file.Stdio.rename;

public final class StdioTest extends FileBaseTest {

  public void testRemoveFile() throws Exception {
    File file = tmp().createFile("a");
    remove(file.getPath());
    assertFalse(file.exists());
  }

  public void testRemoveEmptyDir() throws Exception {
    File dir = tmp().createDir("a");
    remove(dir.getPath());
    assertFalse(dir.exists());
  }

  public void testRemoveNonEmptyDir() throws Exception {
    File file = tmp().createFile("a/b");
    try {
      remove(file.getParent());
      fail();
    } catch (ErrnoException e) {
      // Pass
    }
  }

  public void testRename() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().get("b");
    rename(a.getPath(), b.getPath());
    assertFalse(a.exists());
    assertTrue(b.exists());
  }

  public void testRenameThrowsExceptionOnError() throws Exception {
    try {
      rename(tmp().get("a").getPath(), tmp().get("b").getPath());
      fail();
    } catch (ErrnoException e) {
      // Pass
    }
  }
}
