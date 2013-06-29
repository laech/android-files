package l.files.media;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

public final class MediaMapTest {

  @Test public void getsMediaTypeIgnoringCase() {
    String media1 = new MediaMap().get("txt");
    String media2 = new MediaMap().get("tXT");
    assertThat(media1).isEqualTo(media2);
    assertThat(media1).isNotNull();
  }

}
