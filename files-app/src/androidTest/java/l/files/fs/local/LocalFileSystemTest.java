package l.files.fs.local;

import java.io.File;

import l.files.common.testing.FileBaseTest;
import l.files.fs.FileId;
import l.files.fs.Scheme;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NO_FOLLOW;

public final class LocalFileSystemTest extends FileBaseTest {

  private LocalFileSystem fs;

  @Override protected void setUp() throws Exception {
    super.setUp();
    fs = LocalFileSystem.get();
  }

  public void testScheme() throws Exception {
    assertEquals(Scheme.parse("file"), fs.scheme());
  }

  public void testSymlink() throws Exception {
    File target = tmp().createFile("a");
    File link = tmp().get("b");
    assertFalse(link.exists());

    FileId targetId = FileId.of(target);
    FileId linkId = FileId.of(link);
    fs.symlink(targetId, linkId);

    assertTrue(link.exists());
    assertTrue(fs.stat(linkId, FOLLOW).isRegularFile());
    assertTrue(fs.stat(linkId, NO_FOLLOW).isSymbolicLink());
    assertEquals(
        fs.stat(targetId, NO_FOLLOW).inode(),
        fs.stat(linkId, FOLLOW).inode());
  }
}
