package l.files.ui.browser;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import static android.graphics.PixelFormat.TRANSLUCENT;

final class RoundedColorDrawable extends Drawable {

    private final Paint paint;
    private final RectF rect;
    private final float radius;

    RoundedColorDrawable(float radius) {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.radius = radius;
        this.rect = new RectF(0, 0, 0, 0);
    }

    void setColor(int color) {
        if (paint.getColor() != color) {
            paint.setColor(color);
            invalidateSelf();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        rect.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
        canvas.drawRoundRect(rect, radius, radius, paint);
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

}
