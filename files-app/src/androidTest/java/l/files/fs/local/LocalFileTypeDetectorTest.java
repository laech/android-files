package l.files.fs.local;

import l.files.common.testing.FileBaseTest;
import l.files.fs.FileTypeDetector;
import l.files.fs.Path;

public abstract class LocalFileTypeDetectorTest extends FileBaseTest {

  /**
   * The detector to be tested, using the given file system.
   */
  protected abstract FileTypeDetector detector();

  public void testDetect_directory() throws Exception {
    Path dir = LocalPath.of(tmp().createDir("a"));
    assertEquals("inode/directory", detector().detect(dir).toString());
  }

  public void testDetect_file() throws Exception {
    LocalPath file = LocalPath.of(tmp().createFile("a.txt"));
    assertEquals("text/plain", detector().detect(file).toString());
  }

  public void testDetect_symlinkFile() throws Exception {
    Path file = LocalPath.of(tmp().createFile("a.mp3"));
    Path link = LocalPath.of(tmp().get("b.txt"));
    link.getResource().createSymbolicLink(file);
    assertEquals("text/plain", detector().detect(link).toString());
  }

  public void testDetect_symlinkDirectory() throws Exception {
    Path dir = LocalPath.of(tmp().createDir("a"));
    Path link = LocalPath.of(tmp().get("b"));
    link.getResource().createSymbolicLink(dir);
    assertEquals("inode/directory", detector().detect(link).toString());
  }

}
