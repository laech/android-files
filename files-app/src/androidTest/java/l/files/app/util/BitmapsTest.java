package l.files.app.util;

import junit.framework.TestCase;

import static l.files.app.util.Bitmaps.scaleSize;

public final class BitmapsTest extends TestCase {

  public void testScaleSize() throws Exception {
    ScaledSize size = scaleSize(100, 50, 10, 5);
    assertEquals(.1F, size.scale);
    assertEquals(10, size.scaledWidth);
    assertEquals(5, size.scaledHeight);
  }
}
