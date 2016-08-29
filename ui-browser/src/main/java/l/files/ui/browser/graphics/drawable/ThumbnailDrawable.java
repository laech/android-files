package l.files.ui.browser.graphics.drawable;

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

import javax.annotation.Nullable;

import l.files.ui.browser.R;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static android.graphics.Shader.TileMode.CLAMP;

final class ThumbnailDrawable extends Drawable {

    @Nullable
    private static ColorFilter activatedFilter;

    private static final Paint paint;
    private static final RectF rect;

    static {
        paint = new Paint(ANTI_ALIAS_FLAG);
        rect = new RectF(0, 0, 0, 0);
    }

    @Nullable
    private BitmapShader shader;
    private int width;
    private int height;
    private final float radius;

    private int alpha = 255;

    ThumbnailDrawable(Context context, float radius) {
        this.radius = radius;
        if (activatedFilter == null) {
            activatedFilter = new PorterDuffColorFilter(
                    ContextCompat.getColor(context, R.color.activated_highlight),
                    SRC_ATOP
            );
        }
    }

    void setBitmap(@Nullable Bitmap bitmap) {
        if (bitmap == null) {
            shader = null;
            width = height = -1;
        } else {
            shader = new BitmapShader(bitmap, CLAMP, CLAMP);
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }
        invalidateSelf();
    }

    @Nullable
    public BitmapShader getBitmapShader() {
        return shader;
    }

    @Override
    public void draw(Canvas canvas) {

        if (shader == null) {
            return;
        }

        if (paint.getShader() != shader) {
            paint.setShader(shader);
        }

        if (paint.getAlpha() != alpha) {
            paint.setAlpha(alpha);
        }

        ColorFilter filter = isActivated() ? activatedFilter : null;
        if (paint.getColorFilter() != filter) {
            paint.setColorFilter(filter);
        }

        rect.set(0, 0, width, height);

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
        this.alpha = alpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter filter) {
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
