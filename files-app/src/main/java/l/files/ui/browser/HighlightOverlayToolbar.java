package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Toolbar;

import static android.graphics.Color.parseColor;
import static android.graphics.Paint.Style.STROKE;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

public final class HighlightOverlayToolbar extends Toolbar {

  private static final int LIGHT = parseColor("#11ffffff");

  private final Paint lightPaint;

  {
    Resources res = getContext().getResources();
    float width = applyDimension(COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics());
    lightPaint = new Paint();
    lightPaint.setColor(LIGHT);
    lightPaint.setStrokeWidth(width);
    lightPaint.setStyle(STROKE);
    lightPaint.setAntiAlias(true);
  }

  public HighlightOverlayToolbar(Context context) {
    super(context);
  }

  public HighlightOverlayToolbar(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public HighlightOverlayToolbar(
      Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public HighlightOverlayToolbar(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawLine(0, 0, getWidth(), 0, lightPaint);
  }

}
