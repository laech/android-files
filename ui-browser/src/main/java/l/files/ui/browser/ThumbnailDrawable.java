package l.files.ui.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static android.graphics.Shader.TileMode.CLAMP;

final class ThumbnailDrawable extends Drawable {

    private static ColorFilter activatedFilter;

    private final Paint paint;
    private final RectF rect;
    private final int width;
    private final int height;
    private final float radius;

    ThumbnailDrawable(Context context, float radius, Bitmap bitmap) {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setShader(new BitmapShader(bitmap, CLAMP, CLAMP));
        this.radius = radius;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.rect = new RectF(0, 0, width, height);
        if (activatedFilter == null) {
            activatedFilter = new PorterDuffColorFilter(
                    ContextCompat.getColor(context, R.color.activated_highlight),
                    SRC_ATOP
            );
        }
    }

    @Override
    public void draw(Canvas canvas) {
        paint.setColorFilter(isActivated() ? activatedFilter : null);
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    private boolean isActivated() {
        for (int state : getState()) {
            if (state == android.R.attr.state_activated) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        paint.setColorFilter(filter);
    }

    @Override
    public int getOpacity() {
        return TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

}
