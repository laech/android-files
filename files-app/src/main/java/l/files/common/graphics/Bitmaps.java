package l.files.common.graphics;

import android.graphics.Bitmap;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

public final class Bitmaps {
    private Bitmaps() {
    }

    public static ScaledSize scale(Bitmap bitmap, int maxWidth, int maxHeight) {
        return scale(
                bitmap.getWidth(),
                bitmap.getHeight(),
                maxWidth,
                maxHeight);
    }

    public static ScaledSize scale(
            int width,
            int height,
            int maxWidth,
            int maxHeight) {

        checkGreaterThanZero(width);
        checkGreaterThanZero(height);
        checkGreaterThanZero(maxWidth);
        checkGreaterThanZero(maxHeight);

        float widthRatio = maxWidth / (float) width;
        float heightRatio = maxHeight / (float) height;
        float scale = min(widthRatio, heightRatio);
        int scaledWith = max(round(width * scale), 1);
        int scaledHeight = max(round(height * scale), 1);
        return ScaledSize.of(width, height, scaledWith, scaledHeight, scale);
    }

    private static void checkGreaterThanZero(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    "Must be greater than zero, got " + value);
        }
    }

}
