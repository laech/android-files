package l.files.fs.local;

import java.io.File;

import l.files.common.testing.FileBaseTest;

public final class LocalFileSystemTest extends FileBaseTest {

  private LocalFileSystem fs;

  @Override protected void setUp() throws Exception {
    super.setUp();
    fs = LocalFileSystem.get();
  }

  public void testGetPathFromString() throws Exception {
    assertEquals(LocalPath.of(new File("a")), fs.getPath("a"));
    assertEquals(LocalPath.of(new File("/a")), fs.getPath("/a"));
  }

  public void testGetPathFromUri() throws Exception {
    assertEquals(LocalPath.of(new File("a")), fs.getPath(new File("a").toURI()));
    assertEquals(LocalPath.of(new File("/a")), fs.getPath(new File("/a").toURI()));
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
