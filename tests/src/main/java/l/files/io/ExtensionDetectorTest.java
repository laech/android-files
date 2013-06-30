package l.files.io;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;

import junit.framework.TestCase;

import com.google.common.net.MediaType;

public final class ExtensionDetectorTest extends TestCase {

  private ExtensionDetector medias;

  @Override protected void setUp() throws Exception {
    super.setUp();
    medias = new ExtensionDetector();
  }

  public void testGetsMediaTypeIgnoringCase() {
    MediaType media1 = medias.apply(new File("a.txt"));
    MediaType media2 = medias.apply(new File("b.tXT"));
    assertThat(media1)
        .isEqualTo(media2)
        .isEqualTo(MediaType.parse("text/plain"));
  }

  public void testGetsDefaultMediaTypeForUnknownFileExtension() {
    MediaType media = medias.apply(new File(""));
    assertThat(media).isEqualTo(OCTET_STREAM);
  }
}
