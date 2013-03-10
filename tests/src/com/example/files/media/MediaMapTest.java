package com.example.files.media;

import junit.framework.TestCase;

public final class MediaMapTest extends TestCase {

  private MediaMap medias;

  public void testIgnoresCase() {
    assertNotNull(medias.get("html"));
    assertEquals(medias.get("html"), medias.get("HTML"));
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    medias = new MediaMap();
  }
}
