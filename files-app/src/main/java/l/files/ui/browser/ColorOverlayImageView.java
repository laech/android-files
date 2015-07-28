package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.parseColor;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.Shader.TileMode.CLAMP;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static l.files.R.color.activated_highlight;
import static l.files.R.dimen.files_item_card_radius;

public final class ColorOverlayImageView extends ImageView {

  private static final int LIGHT = parseColor("#44ffffff");
  private static final int DARK = parseColor("#44000000");

  private final int overlayColor;

  private final Paint darkPaint;
  private final float darkSize;

  private final Paint lightPaint;
  private final float lightSize;
  private final float lightRadius;

  {
    Resources res = getContext().getResources();
    overlayColor = res.getColor(activated_highlight);

    lightRadius = res.getDimension(files_item_card_radius);
    lightSize = applyDimension(COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics());
    lightPaint = new Paint();
    lightPaint.setShader(new LinearGradient(
        0,
        0,
        0,
        applyDimension(COMPLEX_UNIT_DIP, 100, res.getDisplayMetrics()),
        LIGHT,
        TRANSPARENT,
        CLAMP));
    lightPaint.setStrokeWidth(lightSize);
    lightPaint.setStyle(STROKE);
    lightPaint.setAntiAlias(true);

    darkSize = 1;
    darkPaint = new Paint();
    darkPaint.setColor(DARK);
  }

  public ColorOverlayImageView(Context context) {
    super(context);
  }

  public ColorOverlayImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ColorOverlayImageView(
      Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ColorOverlayImageView(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (isActivated()) {
      canvas.drawColor(overlayColor);
    }
    drawLight(canvas);
    drawDark(canvas);
  }

  private void drawLight(Canvas canvas) {
    float left = -lightSize / 2;
    float right = getWidth() + lightSize / 2;
    float top = 0;
    float bottom = getHeight();
    canvas.drawRoundRect(
        left, top, right, bottom, lightRadius, lightRadius, lightPaint);
  }

  private void drawDark(Canvas canvas) {
    float startX = 0;
    float startY = getHeight() - darkSize;
    float stopX = getWidth();
    float stopY = getHeight() - darkSize;
    canvas.drawLine(startX, startY, stopX, stopY, darkPaint);
  }
}
