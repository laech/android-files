package l.files.io;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;

import junit.framework.TestCase;

import com.google.common.net.MediaType;

public final class CompositeDetectorTest extends TestCase {

  private static class TestDetector implements MediaTypeDetector {
    private final MediaType result;

    public TestDetector(MediaType result) {
      this.result = result;
    }

    @Override public MediaType apply(File file) {
      return result;
    }
  }

  public void testReturnsDefaultMediaTypeForUnknown() {
    assertThat(new CompositeDetector().apply(new File("")))
        .isEqualTo(OCTET_STREAM);
  }

  public void testReturnsFirstSuccessfulDetectionFromSubDetectors() {
    CompositeDetector detector = new CompositeDetector(
        new TestDetector(OCTET_STREAM),
        new TestDetector(PLAIN_TEXT_UTF_8),
        new TestDetector(OCTET_STREAM));
    assertThat(detector.apply(new File(""))).isEqualTo(PLAIN_TEXT_UTF_8);
  }
}
