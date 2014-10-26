package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;
import l.files.fs.local.FileInfo;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Unistd.symlink;

public final class FileInfoTest extends FileBaseTest {

  public void testSymbolicLink() throws Exception {
    File file = tmp().createDir("file");
    File link = tmp().get("link");
    symlink(file.getPath(), link.getPath());
    assertFalse(info(file).isSymbolicLink());
    assertTrue(info(link).isSymbolicLink());
    assertEquals(lstat(link.getPath()), info(link).stat());
  }

  public void testIsDirectory() throws Exception {
    File dir = tmp().createDir("a");
    assertTrue(info(dir).isDirectory());
  }

  public void testIsRegularFile() throws Exception {
    File file = tmp().createFile("a");
    assertTrue(info(file).isRegularFile());
  }

  public void testInodeNumber() throws Exception {
    File f = tmp().createFile("a");
    assertEquals(lstat(f.getPath()).ino(), info(f).inode());
  }

  public void testDeviceId() throws Exception {
    File f = tmp().createFile("a");
    assertEquals(lstat(f.getPath()).dev(), info(f).device());
  }

  public void testLastModifiedTime() throws Exception {
    FileInfo file = info(tmp().get());
    assertEquals(tmp().get().lastModified(), file.modified());
  }

  public void testReadable() throws Exception {
    File file = tmp().createFile("a");
    assertTrue(file.setReadable(false));
    assertFalse(info(file).isReadable());
    assertTrue(file.setReadable(true));
    assertTrue(info(file).isReadable());
  }

  public void testWritable() throws Exception {
    File file = tmp().createFile("a");
    assertTrue(file.setWritable(false));
    assertFalse(info(file).isWritable());
    assertTrue(file.setWritable(true));
    assertTrue(info(file).isWritable());
  }

  public void testName() throws Exception {
    File file = tmp().createFile("a");
    assertEquals(file.getName(), info(file).name());
  }

  public void testMediaTypeForDirectory() throws Exception {
    File dir = tmp().createDir("a");
    assertEquals("application/x-directory", info(dir).mime());
  }

  public void testMediaTypeForFile() throws Exception {
    File file = tmp().createFile("a.txt");
    assertEquals("text/plain", info(file).mime());
  }

  public void testMediaTypeForSymlinkFile() throws Exception {
    File file = tmp().createFile("a.mp3");
    File link = tmp().get("b.txt");
    symlink(file.getPath(), link.getPath());
    assertEquals("text/plain", info(link).mime());
  }

  public void testMediaTypeForSymlinkDirectory() throws Exception {
    File dir = tmp().createDir("a");
    File link = tmp().get("b");
    symlink(dir.getPath(), link.getPath());
    assertEquals("application/x-directory", info(link).mime());
  }

  public void testDefaultMediaTypeIfUnknown() throws Exception {
    File file = tmp().createFile("a");
    assertEquals("application/octet-stream", info(file).mime());
  }

  public void testSize() throws Exception {
    File file = tmp().createFile("a");
    write("hello world", file, UTF_8);
    assertEquals(file.length(), info(file).size());
  }

  public void testIsHidden() throws Exception {
    assertTrue(info(tmp().createFile(".a")).isHidden());
    assertFalse(info(tmp().createFile("a")).isHidden());
  }

  private FileInfo info(File f) throws IOException {
    return FileInfo.read(f.getPath());
  }

}
