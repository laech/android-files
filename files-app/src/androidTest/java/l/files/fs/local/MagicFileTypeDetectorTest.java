package l.files.fs.local;

import java.io.File;

import l.files.fs.FileSystemException;
import l.files.fs.FileTypeDetector;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static l.files.common.testing.Tests.assertExists;

public final class MagicFileTypeDetectorTest
    extends LocalFileTypeDetectorTest {

  @Override protected FileTypeDetector detector(LocalFileSystem fs) {
    return new MagicFileTypeDetector(fs);
  }

  public void testDetect_returnsOctetStreamForUnreadable() throws Exception {
    File file = tmp().createFile("a.txt");
    write("hello world", file, UTF_8);
    assertTrue(file.setReadable(false));
    try {
      detector().detect(LocalPath.of(file), true);
      fail();
    } catch (FileSystemException e) {
      // Pass
    }
  }

  public void testDetect_returnsOctetStreamForSpecialFile() throws Exception {
    File file = new File("/proc/1/maps");
    assertExists(file);
    try {
      detector().detect(LocalPath.of(file), true);
      fail();
    } catch (FileSystemException e) {
      // Pass
    }
  }
}
