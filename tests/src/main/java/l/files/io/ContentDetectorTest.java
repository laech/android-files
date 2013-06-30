package l.files.io;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static java.io.File.createTempFile;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;

import junit.framework.TestCase;

import com.google.common.net.MediaType;

public final class ContentDetectorTest extends TestCase {

  private File file;
  private ContentDetector detector;

  @Override protected void setUp() throws Exception {
    super.setUp();
    file = createTempFile("abc", "123");
    detector = new ContentDetector();
  }

  @Override protected void tearDown() throws Exception {
    assertTrue(file.delete() || !file.exists());
    super.tearDown();
  }

  public void testDetectsFileContentMediaType() throws Exception {
    write("hello", file, UTF_8);
    MediaType media = detector.apply(file);
    assertThat(media).isEqualTo(MediaType.parse("text/plain"));
  }

  public void testReturnsDefaultMediaTypeForUnknown() {
    MediaType media = detector.apply(file);
    assertThat(media).isEqualTo(OCTET_STREAM);
  }
}
