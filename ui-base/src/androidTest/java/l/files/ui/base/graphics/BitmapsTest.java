package l.files.ui.base.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.BLUE;
import static l.files.ui.base.graphics.Bitmaps.decodeBounds;
import static l.files.ui.base.graphics.Bitmaps.scaleDownBitmap;
import static l.files.ui.base.graphics.Bitmaps.scaleDownOptions;

public final class BitmapsTest extends TestCase {

    public void test_scaleDownBitmap_scale_to_fit() throws Exception {
        Bitmap src = createBitmap(99, 66, BLUE);
        Bitmap expected = createBitmap(33, 22, BLUE);
        Rect max = Rect.of(1000, 22);
        testScaleDownBitmap(src, max, expected);
    }

    public void test_scaleDownBitmap_no_scale_needed() throws Exception {
        Bitmap src = createBitmap(99, 66, BLUE);
        Rect max = Rect.of(1000, 1000);
        testScaleDownBitmap(src, max, src);
    }

    public void testScaleDownBitmap(Bitmap src, Rect max, Bitmap expected) {
        ScaledBitmap result = scaleDownBitmap(src, max);
        assertTrue(result.bitmap().sameAs(expected));
        assertEquals(Rect.of(src), result.originalSize());
    }

    private Bitmap createBitmap(int width, int height, int color) {
        Bitmap src = Bitmap.createBitmap(width, height, ARGB_8888);
        src.eraseColor(color);
        return src;
    }

    public void test_scaleOptions() throws Exception {
        Rect size = Rect.of(20, 20);
        Rect max = Rect.of(10, 10);
        Options options = scaleDownOptions(size, max);
        assertEquals(2, options.inSampleSize);
    }

    public void test_decodeBounds() throws Exception {
        int width = 10;
        int height = 11;
        byte[] bytes = generateBitmapByteArray(width, height);
        Rect bounds = decodeBounds(new ByteArrayInputStream(bytes));
        assertNotNull(bounds);
        assertEquals(width, bounds.width());
        assertEquals(height, bounds.height());
    }

    private byte[] generateBitmapByteArray(int width, int height) {
        Bitmap bitmap = createBitmap(width, height, BLUE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(JPEG, 90, out);
        return out.toByteArray();
    }
}