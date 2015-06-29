package l.files.common.graphics;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.BitmapFactory.Options;
import static android.graphics.BitmapFactory.decodeStream;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Objects.requireNonNull;

public final class Bitmaps
{
    private Bitmaps()
    {
    }

    public static ScaledSize scale(
            final Bitmap bitmap,
            final int maxWidth,
            final int maxHeight)
    {
        return scale(
                bitmap.getWidth(),
                bitmap.getHeight(),
                maxWidth,
                maxHeight);
    }

    public static ScaledSize scale(
            final int width,
            final int height,
            final int maxWidth,
            final int maxHeight)
    {
        checkGreaterThanZero(width);
        checkGreaterThanZero(height);
        checkGreaterThanZero(maxWidth);
        checkGreaterThanZero(maxHeight);

        final float widthRatio = maxWidth / (float) width;
        final float heightRatio = maxHeight / (float) height;
        final float scale = min(widthRatio, heightRatio);
        final int scaledWith = round(width * scale);
        final int scaledHeight = round(height * scale);
        return ScaledSize.of(width, height, scaledWith, scaledHeight, scale);
    }

    private static void checkGreaterThanZero(final int value)
    {
        if (value <= 0)
        {
            throw new IllegalArgumentException(
                    "Must be greater than zero, got " + value);
        }
    }

    /**
     * Decodes an image that can fit within the specified {@link ScaledSize}.
     * Returns null if failed to decode the image.
     */
    @Nullable
    public static Bitmap decode(
            final InputStream in,
            final ScaledSize size) throws IOException
    {
        requireNonNull(in);
        requireNonNull(size);

        final Bitmap bitmap = decodeStream(in, null, options(size));
        if (bitmap == null)
        {
            return null;
        }

        final Bitmap scaled = scale(bitmap, size);
        if (scaled != bitmap)
        {
            bitmap.recycle();
        }

        return scaled;
    }

    private static Options options(final ScaledSize size)
    {
        final Options options = new Options();
        options.inSampleSize = (int) (1 / size.scale());
        return options;
    }

    private static Bitmap scale(
            final Bitmap bitmap,
            final ScaledSize size)
    {
        final int width = size.scaledWidth();
        final int height = size.scaledHeight();
        if (bitmap.getWidth() > width || bitmap.getHeight() > height)
        {
            return createScaledBitmap(bitmap, width, height, true);
        }
        return bitmap;
    }

}
