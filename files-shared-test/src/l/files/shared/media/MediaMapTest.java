package l.files.shared.media;

import junit.framework.TestCase;
import l.files.shared.media.MediaMap;

public final class MediaMapTest extends TestCase {

  public void testGetsMediaTypeIgnoringCase() {
    String media1 = new MediaMap().get("txt");
    String media2 = new MediaMap().get("tXT");
    assertEquals(media1, media2);
    assertNotNull(media1);
  }

}
