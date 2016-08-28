package l.files.ui.base.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.BitmapFactory.decodeStream;

public final class Bitmaps {

    private Bitmaps() {
    }

    @Nullable
    public static Rect decodeBounds(InputStream in) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        decodeStream(in, null, options);
        if (options.outWidth > 0 && options.outHeight > 0) {
            return Rect.of(options.outWidth, options.outHeight);
        }
        return null;
    }

    @Nullable
    public static Rect decodeBounds(Callable<InputStream> provider)
            throws Exception {

        InputStream in = provider.call();
        try {
            return decodeBounds(in);
        } finally {
            in.close();
        }
    }

    /**
     * Decodes a bitmap from the input scaled down to fit {@code max}
     * while maintaining its aspect ratio. The input stream provider
     * must return an input stream at the same initial position every
     * time it's called as multiple pass through the source stream
     * is required.
     */
    @Nullable
    public static ScaledBitmap decodeScaledDownBitmap(
            Callable<InputStream> provider, Rect max) throws Exception {

        Rect originalSize = decodeBounds(provider);
        if (originalSize == null) {
            return null;
        }

        Options opts = scaleDownOptions(originalSize, max);
        Bitmap bitmap;
        InputStream in = provider.call();
        try {
            bitmap = decodeStream(in, null, opts);
        } finally {
            in.close();
        }
        if (bitmap == null) {
            return null;
        }

        ScaledBitmap result = scaleDownBitmap(bitmap, max);
        if (result.bitmap() != bitmap) {
            bitmap.recycle();
        }
        return new ScaledBitmap(result.bitmap(), originalSize);
    }

    /**
     * Returns options with {@link Options#inSampleSize} set
     * to the appropriate value by scaling {@code size} so that
     * it fits within {@code max} while maintaining original
     * aspect ratio.
     */
    public static Options scaleDownOptions(Rect size, Rect max) {
        Rect scaled = size.scaleDown(max);
        float scale = scaled.width() / (float) size.width();
        Options options = new Options();
        options.inSampleSize = (int) (1 / scale);
        return options;
    }

    /**
     * Scales the bitmap to fit within {@code max} while
     * maintaining its aspect ratio.
     */
    public static ScaledBitmap scaleDownBitmap(Bitmap src, Rect max) {
        Rect originalSize = Rect.of(src.getWidth(), src.getHeight());
        Rect scaledSize = originalSize.scaleDown(max);
        Bitmap scaledBitmap = createScaledBitmap(
                src,
                scaledSize.width(),
                scaledSize.height(),
                true);
        return new ScaledBitmap(scaledBitmap, originalSize);
    }

}
