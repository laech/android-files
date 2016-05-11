package l.files.ui.preview;

import android.graphics.Bitmap;

import static l.files.base.Objects.requireNonNull;

final class BlurredThumbnail {

    final Bitmap bitmap;

    BlurredThumbnail(Bitmap bitmap) {
        this.bitmap = requireNonNull(bitmap);
    }

}
