package l.files.common.graphics;

import android.graphics.Bitmap;

import static java.lang.Math.min;
import static java.lang.Math.round;

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

}
