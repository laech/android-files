package com.example.files.media;

import junit.framework.TestCase;

public final class MediaMapTest extends TestCase {

  public void testGetsMediaTypeIgnoringCase() {
    String media1 = new MediaMap().get("txt");
    String media2 = new MediaMap().get("tXT");
    assertEquals(media1, media2);
    assertNotNull(media1);
  }

}
