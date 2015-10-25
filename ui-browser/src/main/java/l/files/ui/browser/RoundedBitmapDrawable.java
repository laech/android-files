package l.files.ui.browser;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.graphics.Shader.TileMode.CLAMP;

final class RoundedBitmapDrawable extends Drawable {

    private final Paint paint;
    private final RectF rect;
    private final int width;
    private final int height;
    private final float radius;

    RoundedBitmapDrawable(float radius, Bitmap bitmap) {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setShader(new BitmapShader(bitmap, CLAMP, CLAMP));
        this.radius = radius;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.rect = new RectF(0, 0, width, height);
    }

    @Override
    public void draw(Canvas canvas) {
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

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

}
