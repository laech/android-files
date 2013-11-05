package l.files.common.io;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.io.File.createTempFile;

public final class ContentDetectorTest extends TestCase {

  private File file;

  @Override protected void setUp() throws Exception {
    super.setUp();
    file = createTempFile("abc", "123");
  }

  @Override protected void tearDown() throws Exception {
    assertTrue(file.delete() || !file.exists());
    super.tearDown();
  }

  public void testDetectsFileContentMediaType() throws Exception {
    write("hello", file, UTF_8);
    FileInputStream stream = new FileInputStream(file);
    try {
      assertEquals("text/plain", ContentDetector.INSTANCE.detect(stream));
    } finally {
      stream.close();
    }
  }

  public void testReturnsDefaultMediaTypeForUnknown() throws IOException {
    FileInputStream stream = new FileInputStream(file);
    try {
      assertEquals("application/octet-stream",
          ContentDetector.INSTANCE.detect(stream));
    } finally {
      stream.close();
    }
  }
}
