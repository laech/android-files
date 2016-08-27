package l.files.ui.base.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import l.files.base.io.Closer;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.graphics.Bitmap.CompressFormat.PNG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.BLUE;
import static l.files.ui.base.graphics.Bitmaps.decodeBounds;
import static l.files.ui.base.graphics.Bitmaps.decodeScaledDownBitmap;
import static l.files.ui.base.graphics.Bitmaps.scaleDownBitmap;
import static l.files.ui.base.graphics.Bitmaps.scaleDownOptions;

public final class BitmapsTest extends TestCase {

    public void test_decodeScaledDownBitmap_scale_to_fit() throws Exception {
        Bitmap src = createBitmap(100, 100, BLUE);
        Bitmap expected = createBitmap(99, 99, BLUE);
        Rect max = Rect.of(1000, 99);
        testDecodeScaledDownBitmap(src, max, expected);
    }

    public void test_decodeScaledDownBitmap_no_scale_needed() throws Exception {
        Bitmap src = createBitmap(99, 66, BLUE);
        Rect max = Rect.of(1000, 1000);
        testDecodeScaledDownBitmap(src, max, src);
    }

    private void testDecodeScaledDownBitmap(Bitmap src, Rect max, Bitmap expected)
            throws IOException {

        Closer closer = Closer.create();
        try {

            File file = createTempFile(closer);
            write(src, file);

            InputStream in = closer.register(new FileInputStream(file));
            ScaledBitmap result = decodeScaledDownBitmap(in, max);

            assertNotNull(result);
            assertTrue(result.bitmap().sameAs(expected));
            assertEquals(Rect.of(src), result.originalSize());

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private static void write(Bitmap src, File file) throws IOException {
        Closer closer = Closer.create();
        try {
            OutputStream out = closer.register(new FileOutputStream(file));
            src.compress(PNG, 100, out);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private static File createTempFile(Closer closer) throws IOException {
        final File file = File.createTempFile("BitmapsTest", null);
        closer.register(new Closeable() {
            @Override
            public void close() throws IOException {
                assertTrue(file.delete());
            }
        });
        return file;
    }

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