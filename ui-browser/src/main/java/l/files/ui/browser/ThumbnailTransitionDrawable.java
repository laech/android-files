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
    private final float cornerRadius;

    private boolean showingBitmap;

    ThumbnailTransitionDrawable(Context context, float cornerRadius) {
        super(new Drawable[]{
                new SizedColorDrawable(TRANSPARENT),
                new ThumbnailDrawable(context, cornerRadius)
        });
        this.cornerRadius = cornerRadius;
        this.sizedColorDrawable = (SizedColorDrawable) getDrawable(0);
        this.thumbnailDrawable = (ThumbnailDrawable) getDrawable(1);
    }

    float getCornerRadius() {
        return cornerRadius;
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

    boolean isShowingBitmap() {
        return showingBitmap;
    }

}
