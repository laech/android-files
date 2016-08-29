package l.files.ui.browser.graphics.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import javax.annotation.Nullable;

import l.files.ui.preview.SizedColorDrawable;

import static android.graphics.Color.TRANSPARENT;

public final class ThumbnailTransitionDrawable extends TransitionDrawable {

    private final SizedColorDrawable sizedColorDrawable;
    private final ThumbnailDrawable thumbnailDrawable;

    private boolean showingBitmap;

    public ThumbnailTransitionDrawable(Context context, float cornerRadius) {
        super(new Drawable[]{
                new SizedColorDrawable(TRANSPARENT),
                new ThumbnailDrawable(context, cornerRadius)
        });
        this.sizedColorDrawable = (SizedColorDrawable) getDrawable(0);
        this.thumbnailDrawable = (ThumbnailDrawable) getDrawable(1);
    }

    public void setSize(int width, int height) {
        sizedColorDrawable.setSize(width, height);
        thumbnailDrawable.setBitmap(null);
    }

    public void setBitmap(@Nullable Bitmap bitmap) {
        thumbnailDrawable.setBitmap(bitmap);
        if (bitmap == null) {
            sizedColorDrawable.setSize(0, 0);
        } else {
            sizedColorDrawable.setSize(bitmap.getWidth(), bitmap.getHeight());
        }
    }

    public boolean hasVisibleContent() {
        return (sizedColorDrawable.getIntrinsicWidth() > 0 &&
                sizedColorDrawable.getIntrinsicHeight() > 0) ||
                thumbnailDrawable.getBitmapShader() != null;
    }

    @Override
    public void startTransition(int durationMillis) {
        super.startTransition(durationMillis);
        showingBitmap = true;
    }

    @Override
    public void resetTransition() {
        super.resetTransition();
        showingBitmap = false;
    }

    public boolean isShowingBitmap() {
        return showingBitmap;
    }

}
