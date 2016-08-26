package l.files.ui.base.graphics;

import android.graphics.BitmapFactory.Options;

import java.io.InputStream;

import javax.annotation.Nullable;

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

    /**
     * Returns options with {@link Options#inSampleSize} set
     * to the appropriate value by scaling {@code size} so that
     * it fits within {@code max} while maintaining original
     * aspect ratio.
     */
    public static Options scaleOptions(Rect size, Rect max) {
        Rect scaled = size.scale(max);
        float scale = scaled.width() / (float) size.width();
        Options options = new Options();
        options.inSampleSize = (int) (1 / scale);
        return options;
    }

}
