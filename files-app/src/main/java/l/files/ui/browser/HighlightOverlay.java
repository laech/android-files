package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import static android.graphics.Color.parseColor;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.Shader.TileMode.CLAMP;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static l.files.R.dimen.files_item_card_radius;

public final class HighlightOverlay extends View {

  private static final int LIGHT = parseColor("#44ffffff");
  private static final int DARK = parseColor("#22000000");

  private final Paint lightPaint;
  private final float lightSize;
  private final float lightRadius;

  {
    Resources res = getContext().getResources();

    lightRadius = res.getDimension(files_item_card_radius);
    lightSize = applyDimension(COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics());
    lightPaint = new Paint();
    lightPaint.setShader(new LinearGradient(
        0,
        0,
        0,
        applyDimension(COMPLEX_UNIT_DIP, 100, res.getDisplayMetrics()),
        LIGHT,
        DARK,
        CLAMP));
    lightPaint.setStrokeWidth(lightSize);
    lightPaint.setStyle(STROKE);
    lightPaint.setAntiAlias(true);
  }

  public HighlightOverlay(Context context) {
    super(context);
  }

  public HighlightOverlay(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public HighlightOverlay(
      Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public HighlightOverlay(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawLight(canvas);
  }

  private void drawLight(Canvas canvas) {
    float left = -lightSize / 3;
    float right = getWidth() + lightSize / 3;
    float top = 0;
    float bottom = getHeight();
    canvas.drawRoundRect(
        left, top, right, bottom, lightRadius, lightRadius, lightPaint);
  }

}
