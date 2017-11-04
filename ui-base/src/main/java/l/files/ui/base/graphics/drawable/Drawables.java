package l.files.ui.base.graphics.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import android.support.annotation.Nullable;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;

public final class Drawables {

    private Drawables() {
    }

    /**
     * Creates a bitmap from a drawable, no bigger than {@code max},
     * aspect ratio is maintained. If the drawable has no intrinsic
     * width or height, null is returned.
     */
    @Nullable
    public static ScaledBitmap toBitmap(Drawable drawable, Rect max) {

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        if (intrinsicWidth < 0 || intrinsicHeight < 0) {
            return null;
        }

        Rect originalSize = Rect.of(intrinsicWidth, intrinsicHeight);
        Rect scaledSize = originalSize.scaleDown(max);
        Bitmap bitmap = createBitmap(scaledSize.width(), scaledSize.height(), ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return new ScaledBitmap(bitmap, originalSize);
    }

}
