package l.files.io;

import com.google.common.base.Function;
import com.google.common.net.MediaType;
import junit.framework.TestCase;

import java.io.File;

import static com.google.common.net.MediaType.OCTET_STREAM;
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class CompositeDetectorTest extends TestCase {

  private Function<File, MediaType> detector1;
  private Function<File, MediaType> detector2;

  private Function<File, MediaType> composite;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    detector1 = mock(Function.class);
    detector2 = mock(Function.class);
    composite = new CompositeDetector(detector1, detector2);
  }

  public void testReturnsFirstNonOctetStreamMediaType() {
    test(
        OCTET_STREAM,
        PLAIN_TEXT_UTF_8,
        PLAIN_TEXT_UTF_8);
  }

  public void testReturnsFirstNonNullMediaType() {
    test(
        null,
        PLAIN_TEXT_UTF_8,
        PLAIN_TEXT_UTF_8);
  }

  public void testReturnsOctetStreamIfAllInnerDetectorsReturnNull() {
    test(
        null,
        null,
        OCTET_STREAM);
  }

  public void testReturnsOctetStreamIfAllInnerDetectorsReturnOctetStream() {
    test(
        OCTET_STREAM,
        OCTET_STREAM,
        OCTET_STREAM);
  }

  private void test(
      MediaType forDetector1,
      MediaType forDetector2,
      MediaType expected) {
    File file = mock(File.class);
    given(detector1.apply(file)).willReturn(forDetector1);
    given(detector2.apply(file)).willReturn(forDetector2);
    assertThat(composite.apply(file)).isEqualTo(expected);
  }
}
