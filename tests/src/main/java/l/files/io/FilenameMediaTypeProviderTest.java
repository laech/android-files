package l.files.io;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;

import junit.framework.TestCase;

import com.google.common.base.Optional;

public final class FilenameMediaTypeProviderTest extends TestCase {

  private FilenameMediaTypeProvider medias;

  @Override protected void setUp() throws Exception {
    super.setUp();
    medias = new FilenameMediaTypeProvider();
  }

  public void testGetsMediaTypeIgnoringCase() {
    Optional<String> media1 = medias.apply(new File("a.txt"));
    Optional<String> media2 = medias.apply(new File("b.tXT"));
    assertThat(media1).isEqualTo(media2);
    assertThat(media1.isPresent()).isTrue();
  }

}
