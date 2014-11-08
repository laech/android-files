package l.files.fs.local;

import l.files.common.testing.FileBaseTest;

public final class LocalFileSystemTest extends FileBaseTest {

  private LocalFileSystem fs;

  @Override protected void setUp() throws Exception {
    super.setUp();
    fs = LocalFileSystem.get();
  }

  public void testScheme() throws Exception {
    assertTrue(LocalFileSystem.canHandle(LocalPath.of("/")));
  }

  public void testSymlink() throws Exception {
    LocalPath target = LocalPath.of(tmp().createFile("a"));
    LocalPath link = LocalPath.of(tmp().get("b"));
    assertFalse(link.toFile().exists());

    fs.symlink(target, link);

    assertTrue(link.toFile().exists());
    assertTrue(fs.stat(link, true).isRegularFile());
    assertTrue(fs.stat(link, false).isSymbolicLink());
    assertEquals(
        fs.stat(target, false).inode(),
        fs.stat(link, true).inode());
  }
}
