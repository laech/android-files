package l.files.base.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static l.files.base.graphics.Bitmaps.decodeBounds;
import static l.files.base.graphics.Bitmaps.scaleOptions;

public final class BitmapsTest extends TestCase {

    public void test_scaleOptions() throws Exception {
        Rect size = Rect.of(20, 20);
        Rect max = Rect.of(10, 10);
        Options options = scaleOptions(size, max);
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
        Bitmap bitmap = createBitmap(width, height, ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(JPEG, 90, out);
        return out.toByteArray();
    }
}