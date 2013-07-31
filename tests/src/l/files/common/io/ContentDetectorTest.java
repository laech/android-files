package l.files.common.io;

import com.google.common.net.MediaType;
import junit.framework.TestCase;

import java.io.File;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static com.google.common.net.MediaType.OCTET_STREAM;
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
    assertEquals(MediaType.parse("text/plain"), ContentDetector.INSTANCE.detect(file));
  }

  public void testReturnsDefaultMediaTypeForUnknown() {
    assertEquals(OCTET_STREAM, ContentDetector.INSTANCE.detect(file));
  }
}
