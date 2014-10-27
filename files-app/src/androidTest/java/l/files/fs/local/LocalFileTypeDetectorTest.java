package l.files.fs.local;

import java.io.File;

import l.files.common.testing.FileBaseTest;
import l.files.fs.FileId;
import l.files.fs.FileTypeDetector;

import static l.files.fs.Files.symlink;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NO_FOLLOW;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public abstract class LocalFileTypeDetectorTest extends FileBaseTest {

  /**
   * The detector to be tested, using the given file system.
   */
  protected abstract FileTypeDetector detector(LocalFileSystem fs);

  protected FileTypeDetector detector() {
    return detector(LocalFileSystem.get());
  }

  public void testDetect_directory() throws Exception {
    FileId dir = FileId.of(tmp().createDir("a"));
    assertEquals("inode/directory", detector().detect(dir, NO_FOLLOW).toString());
  }

  public void testDetect_file() throws Exception {
    FileId file = FileId.of(tmp().createFile("a.txt"));
    assertEquals("text/plain", detector().detect(file, NO_FOLLOW).toString());
  }

  public void testDetect_symlinkFile() throws Exception {
    FileId file = FileId.of(tmp().createFile("a.mp3"));
    FileId link = FileId.of(tmp().get("b.txt"));
    symlink(file, link);
    assertEquals("text/plain", detector().detect(link, FOLLOW).toString());
  }

  public void testDetect_symlinkDirectory() throws Exception {
    FileId dir = FileId.of(tmp().createDir("a"));
    FileId link = FileId.of(tmp().get("b"));
    symlink(dir, link);
    assertEquals("inode/directory", detector().detect(link, FOLLOW).toString());
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

  private void testDetectSpecialFile(String expectedMime, LocalFileStatus stat) {
    FileId file = FileId.of(new File("test"));
    LocalFileSystem fs = mock(LocalFileSystem.class);
    given(fs.stat(file, NO_FOLLOW)).willReturn(stat);
    assertEquals(expectedMime, detector(fs).detect(file, NO_FOLLOW).toString());
  }
}
