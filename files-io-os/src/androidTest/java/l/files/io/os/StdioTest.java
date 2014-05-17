package l.files.io.os;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static l.files.io.os.Stdio.rename;

public final class StdioTest extends FileBaseTest {

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
