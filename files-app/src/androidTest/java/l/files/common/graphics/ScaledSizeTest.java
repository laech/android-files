package l.files.common.graphics;

import junit.framework.TestCase;

public final class ScaledSizeTest extends TestCase {
    public void test_properties_return_original_value() throws Exception {
        final int originalWidth = 1;
        final int originalHeight = 2;
        final int scaledWidth = 3;
        final int scaledHeight = 4;
        final float scale = 0.5f;

        final ScaledSize actual = ScaledSize.of(
                originalWidth,
                originalHeight,
                scaledWidth,
                scaledHeight,
                scale);

        assertEquals(originalWidth, actual.originalWidth());
        assertEquals(originalHeight, actual.originalHeight());
        assertEquals(scaledWidth, actual.scaledWidth());
        assertEquals(scaledHeight, actual.scaledHeight());
        assertEquals(scale, actual.scale());
    }
}
