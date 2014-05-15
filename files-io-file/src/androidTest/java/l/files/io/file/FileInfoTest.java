package l.files.io.file;

import l.files.common.testing.FileBaseTest;
import l.files.io.os.Stat;

import static l.files.io.file.FileInfo.exists;
import static l.files.io.os.Stat.lstat;
import static l.files.io.os.Unistd.symlink;

public final class FileInfoTest extends FileBaseTest {

  public void testExistence() throws Exception {
    assertFalse(exists("/abc/def/123"));
    assertTrue(exists("/"));
  }

  public void testGetsSymbolicLinkInfo() throws Exception {
    String a = tmp().createDir("a").getPath();
    String b = tmp().get("b").getPath();
    symlink(a, b);

    Stat expected = lstat(b);
    Stat actual = FileInfo.get(b).stat();
    assertEquals(expected, actual);
  }

  public void testGetsLastModifiedTime() throws Exception {
    FileInfo file = FileInfo.get(tmp().get().getPath());
    assertEquals(tmp().get().lastModified(), file.lastModified());
  }
}
