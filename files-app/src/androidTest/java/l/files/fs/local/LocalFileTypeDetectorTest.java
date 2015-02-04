package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;
import l.files.fs.FileTypeDetector;
import l.files.fs.Path;
import l.files.fs.Resource;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public abstract class LocalFileTypeDetectorTest extends FileBaseTest {

  /**
   * The detector to be tested, using the given file system.
   */
  protected abstract FileTypeDetector detector();

  public void testDetect_directory() throws Exception {
    Path dir = LocalPath.of(tmp().createDir("a"));
    assertEquals("inode/directory", detector().detect(dir, false).toString());
  }

  public void testDetect_file() throws Exception {
    LocalPath file = LocalPath.of(tmp().createFile("a.txt"));
    assertEquals("text/plain", detector().detect(file, false).toString());
  }

  public void testDetect_symlinkFile() throws Exception {
    Path file = LocalPath.of(tmp().createFile("a.mp3"));
    Path link = LocalPath.of(tmp().get("b.txt"));
    link.getResource().createSymbolicLink(file);
    assertEquals("text/plain", detector().detect(link, true).toString());
  }

  public void testDetect_symlinkDirectory() throws Exception {
    Path dir = LocalPath.of(tmp().createDir("a"));
    Path link = LocalPath.of(tmp().get("b"));
    link.getResource().createSymbolicLink(dir);
    assertEquals("inode/directory", detector().detect(link, true).toString());
  }

  public void testDetect_fifo() throws Exception {
    LocalFileStatus stat = mock(LocalFileStatus.class);
    given(stat.isFifo()).willReturn(true);
    testDetectSpecialFile("inode/fifo", stat);
  }

  public void testDetect_blockDevice() throws Exception {
    LocalFileStatus stat = mock(LocalFileStatus.class);
    given(stat.isBlockDevice()).willReturn(true);
    testDetectSpecialFile("inode/blockdevice", stat);
  }

  public void testDetect_charDevice() throws Exception {
    LocalFileStatus stat = mock(LocalFileStatus.class);
    given(stat.isCharacterDevice()).willReturn(true);
    testDetectSpecialFile("inode/chardevice", stat);
  }

  public void testDetect_socket() throws Exception {
    LocalFileStatus stat = mock(LocalFileStatus.class);
    given(stat.isSocket()).willReturn(true);
    testDetectSpecialFile("inode/socket", stat);
  }

  public void testDetect_symlink() throws Exception {
    LocalFileStatus stat = mock(LocalFileStatus.class);
    given(stat.isSymbolicLink()).willReturn(true);
    testDetectSpecialFile("inode/symlink", stat);
  }

  private void testDetectSpecialFile(String expectedMime, LocalFileStatus stat)
      throws IOException {
    Path file = LocalPath.of(new File("test"));
    Resource fs = mock(Resource.class);
    given(fs.stat()).willReturn(stat);
    assertEquals(expectedMime, detector().detect(file, false).toString());
  }
}
