package l.files.ui.base.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.graphics.Bitmap.CompressFormat.PNG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.BLUE;
import static java.io.File.createTempFile;
import static l.files.ui.base.graphics.Bitmaps.decodeScaledDownBitmap;
import static l.files.ui.base.graphics.Bitmaps.scaleDownBitmap;
import static l.files.ui.base.graphics.Bitmaps.scaleDownOptions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class BitmapsTest {

    @Test
    public void decodeScaledDownBitmap_scale_to_fit() throws Exception {
        Bitmap src = createBitmap(100, 100, BLUE);
        Bitmap expected = createBitmap(99, 99, BLUE);
        Rect max = Rect.of(1000, 99);
        testDecodeScaledDownBitmap(src, max, expected);
    }

    @Test
    public void decodeScaledDownBitmap_no_scale_needed() throws Exception {
        Bitmap src = createBitmap(99, 66, BLUE);
        Rect max = Rect.of(1000, 1000);
        testDecodeScaledDownBitmap(src, max, src);
    }

    private void testDecodeScaledDownBitmap(Bitmap src, Rect max, Bitmap expected)
            throws Exception {

        final File file = createTempFile("BitmapsTest", null);
        try {
            write(src, file);
            ScaledBitmap result = decodeScaledDownBitmap(new Callable<InputStream>() {
                @Override
                public InputStream call() throws IOException {
                    return new FileInputStream(file);
                }
            }, max);

            assertNotNull(result);
            assertTrue(result.bitmap().sameAs(expected));
            assertEquals(Rect.of(src), result.originalSize());

        } finally {
            assertTrue(file.delete() || !file.exists());
        }
    }

    private static void write(Bitmap src, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            src.compress(PNG, 100, out);
        } finally {
            out.close();
        }
    }

    @Test
    public void scaleDownBitmap_scale_to_fit() throws Exception {
        Bitmap src = createBitmap(99, 66, BLUE);
        Bitmap expected = createBitmap(33, 22, BLUE);
        Rect max = Rect.of(1000, 22);
        testScaleDownBitmap(src, max, expected);
    }

    @Test
    public void scaleDownBitmap_no_scale_needed() throws Exception {
        Bitmap src = createBitmap(99, 66, BLUE);
        Rect max = Rect.of(1000, 1000);
        testScaleDownBitmap(src, max, src);
    }

    private void testScaleDownBitmap(Bitmap src, Rect max, Bitmap expected) {
        ScaledBitmap result = scaleDownBitmap(src, max);
        assertTrue(result.bitmap().sameAs(expected));
        assertEquals(Rect.of(src), result.originalSize());
    }

    private Bitmap createBitmap(int width, int height, int color) {
        Bitmap src = Bitmap.createBitmap(width, height, ARGB_8888);
        src.eraseColor(color);
        return src;
    }

    @Test
    public void scaleOptions() throws Exception {
        Rect size = Rect.of(20, 20);
        Rect max = Rect.of(10, 10);
        Options options = scaleDownOptions(size, max);
        assertEquals(2, options.inSampleSize);
    }

    @Test
    public void decodeBounds() throws Exception {
        final int width = 10;
        final int height = 11;
        final byte[] bytes = generateBitmapByteArray(width, height);
        final Rect bounds = Bitmaps.decodeBounds(new Callable<InputStream>() {
            @Override
            public InputStream call() {
                return new ByteArrayInputStream(bytes);
            }
        });
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