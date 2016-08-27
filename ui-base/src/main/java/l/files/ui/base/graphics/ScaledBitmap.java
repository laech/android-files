package l.files.ui.base.graphics;

import android.graphics.Bitmap;

import static l.files.base.Objects.requireNonNull;

public final class ScaledBitmap {

    private final Bitmap bitmap;
    private final Rect originalSize;

    public ScaledBitmap(Bitmap bitmap, Rect originalSize) {
        this.bitmap = requireNonNull(bitmap, "bitmap");
        this.originalSize = requireNonNull(originalSize, "originalSize");
    }

    public Bitmap bitmap() {
        return bitmap;
    }

    public Rect originalSize() {
        return originalSize;
    }

}
