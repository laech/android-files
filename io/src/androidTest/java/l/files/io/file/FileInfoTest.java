package l.files.io.file;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static l.files.io.file.Stat.lstat;
import static l.files.io.file.Unistd.symlink;
import static org.assertj.core.api.Assertions.assertThat;

public final class FileInfoTest extends FileBaseTest {

  public void testSymbolicLink() throws Exception {
    File file = tmp().createDir("file");
    File link = tmp().get("link");
    symlink(file.getPath(), link.getPath());
    assertThat(info(file).isSymbolicLink()).isFalse();
    assertThat(info(link).isSymbolicLink()).isTrue();
    assertThat(info(link).stat()).isEqualTo(lstat(link.getPath()));
  }

  public void testIsDirectory() throws Exception {
    File dir = tmp().createDir("a");
    assertThat(info(dir).isDirectory()).isTrue();
  }

  public void testIsRegularFile() throws Exception {
    File file = tmp().createFile("a");
    assertThat(info(file).isRegularFile()).isTrue();
  }

  public void testInodeNumber() throws Exception {
    File f = tmp().createFile("a");
    assertThat(info(f).inode()).isEqualTo(lstat(f.getPath()).ino);
  }

  public void testDeviceId() throws Exception {
    File f = tmp().createFile("a");
    assertThat(info(f).device()).isEqualTo(lstat(f.getPath()).dev);
  }

  public void testLastModifiedTime() throws Exception {
    FileInfo file = info(tmp().get());
    assertEquals(tmp().get().lastModified(), file.modified());
  }

  public void testReadable() throws Exception {
    File file = tmp().createFile("a");
    assertThat(file.setReadable(false)).isTrue();
    assertThat(info(file).isReadable()).isFalse();
    assertThat(file.setReadable(true)).isTrue();
    assertThat(info(file).isReadable()).isTrue();
  }

  public void testWritable() throws Exception {
    File file = tmp().createFile("a");
    assertThat(file.setWritable(false)).isTrue();
    assertThat(info(file).isWritable()).isFalse();
    assertThat(file.setWritable(true)).isTrue();
    assertThat(info(file).isWritable()).isTrue();
  }

  public void testName() throws Exception {
    File file = tmp().createFile("a");
    assertThat(info(file).name()).isEqualTo(file.getName());
  }

  public void testMediaTypeForDirectory() throws Exception {
    File dir = tmp().createDir("a");
    assertThat(info(dir).mime()).isEqualTo("application/x-directory");
  }

  public void testMediaTypeForFile() throws Exception {
    File file = tmp().createFile("a.txt");
    assertThat(info(file).mime()).isEqualTo("text/plain");
  }

  public void testUri() throws Exception {
    File file = tmp().createFile("a");
    assertThat(info(file).uri())
        .isEqualTo("file://" + file.getAbsolutePath());
  }

  public void testSize() throws Exception {
    File file = tmp().createFile("a");
    write("hello world", file, UTF_8);
    assertThat(info(file).size()).isEqualTo(file.length());
  }

  public void testIsHidden() throws Exception {
    assertThat(info(tmp().createFile(".a")).isHidden()).isTrue();
    assertThat(info(tmp().createFile("a")).isHidden()).isFalse();
  }

  private FileInfo info(File f) throws IOException {
    return FileInfo.read(f.getPath());
  }

}
