package l.files.ui.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import l.files.ui.preview.SizedColorDrawable;

import static android.graphics.Color.TRANSPARENT;

final class ThumbnailTransitionDrawable extends TransitionDrawable {

    private final SizedColorDrawable sizedColorDrawable;
    private final ThumbnailDrawable thumbnailDrawable;

    ThumbnailTransitionDrawable(Context context, float cornerRadius) {
        super(new Drawable[]{
                new SizedColorDrawable(TRANSPARENT),
                new ThumbnailDrawable(context, cornerRadius)
        });
        sizedColorDrawable = (SizedColorDrawable) getDrawable(0);
        thumbnailDrawable = (ThumbnailDrawable) getDrawable(1);
    }

    void setSize(int width, int height) {
        sizedColorDrawable.setSize(width, height);
    }

    void setBitmap(Bitmap bitmap) {
        thumbnailDrawable.setBitmap(bitmap);
        if (bitmap == null) {
            setSize(0, 0);
        } else {
            setSize(bitmap.getWidth(), bitmap.getHeight());
        }
    }

    boolean hasVisibleContent() {
        return sizedColorDrawable.getIntrinsicWidth() > 0 &&
                sizedColorDrawable.getIntrinsicHeight() > 0 ||
                thumbnailDrawable.getBitmapShader() != null;
    }

}
