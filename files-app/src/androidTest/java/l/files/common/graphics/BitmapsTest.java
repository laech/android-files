package l.files.common.graphics;

import junit.framework.TestCase;

import static l.files.common.graphics.Bitmaps.scale;

public final class BitmapsTest extends TestCase
{
    public void test_scale_size_maintains_aspect_ratio() throws Exception
    {
        final ScaledSize size = scale(100, 50, 10, 5);
        assertEquals(.1F, size.scale());
        assertEquals(10, size.scaledWidth());
        assertEquals(5, size.scaledHeight());
    }
}
