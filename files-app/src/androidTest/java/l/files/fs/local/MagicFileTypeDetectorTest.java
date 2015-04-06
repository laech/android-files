package l.files.fs.local;

import java.io.File;
import java.io.IOException;

import l.files.fs.FileTypeDetector;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static l.files.common.testing.Tests.assertExists;

public final class MagicFileTypeDetectorTest extends LocalFileTypeDetectorTest {

  @Override protected FileTypeDetector detector() {
    return MagicFileTypeDetector.INSTANCE;
  }

  public void testDetect_returnsOctetStreamForUnreadable() throws Exception {
    File file = tmp().createFile("a.txt");
    write("hello world", file, UTF_8);
    assertTrue(file.setReadable(false));
    try {
      detector().detect(LocalPath.of(file));
      fail();
    } catch (IOException e) {
      // Pass
    }
  }

  public void testDetect_returnsOctetStreamForSpecialFile() throws Exception {
    File file = new File("/proc/1/maps");
    assertExists(file);
    try {
      detector().detect(LocalPath.of(file));
      fail();
    } catch (IOException e) {
      // Pass
    }
  }
}
