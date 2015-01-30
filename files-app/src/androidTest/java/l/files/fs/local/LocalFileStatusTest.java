package l.files.fs.local;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static l.files.fs.local.LocalFileStatus.stat;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Unistd.symlink;

public final class LocalFileStatusTest extends FileBaseTest {

  public void testSymbolicLink() throws Exception {
    File file = tmp().createDir("file");
    File link = tmp().get("link");
    symlink(file.getPath(), link.getPath());
    assertFalse(stat(file, false).isSymbolicLink());
    assertFalse(stat(link, true).isSymbolicLink());
    assertTrue(stat(link, false).isSymbolicLink());
    assertEquals(Stat.lstat(link.getPath()), stat(link, false).stat());
    assertEquals(Stat.stat(link.getPath()), stat(link, true).stat());
  }

  public void testIsDirectory() throws Exception {
    File dir = tmp().createDir("a");
    assertTrue(stat(dir, false).isDirectory());
  }

  public void testIsRegularFile() throws Exception {
    File file = tmp().createFile("a");
    assertTrue(stat(file, false).isRegularFile());
  }

  public void testInodeNumber() throws Exception {
    File f = tmp().createFile("a");
    assertEquals(lstat(f.getPath()).ino(), stat(f, false).inode());
  }

  public void testDeviceId() throws Exception {
    File f = tmp().createFile("a");
    assertEquals(lstat(f.getPath()).dev(), stat(f, false).device());
  }

  public void testLastModifiedTime() throws Exception {
    LocalFileStatus file = stat(tmp().get(), false);
    assertEquals(tmp().get().lastModified(), file.lastModifiedTime());
  }

  public void testReadable() throws Exception {
    File file = tmp().createFile("a");
    assertTrue(file.setReadable(false));
    assertFalse(stat(file, false).isReadable());
    assertTrue(file.setReadable(true));
    assertTrue(stat(file, false).isReadable());
  }

  public void testWritable() throws Exception {
    File file = tmp().createFile("a");
    assertTrue(file.setWritable(false));
    assertFalse(stat(file, false).isWritable());
    assertTrue(file.setWritable(true));
    assertTrue(stat(file, false).isWritable());
  }

  public void testName() throws Exception {
    File file = tmp().createFile("a");
    assertEquals(file.getName(), stat(file, false).name());
  }

  public void testSize() throws Exception {
    File file = tmp().createFile("a");
    write("hello world", file, UTF_8);
    assertEquals(file.length(), stat(file, false).size());
  }

  public void testIsHidden() throws Exception {
    assertTrue(stat(tmp().createFile(".a"), false).isHidden());
    assertFalse(stat(tmp().createFile("a"), false).isHidden());
  }
}
