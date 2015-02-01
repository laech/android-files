package l.files.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class CircleTextView extends TextView {

  private final Paint paint;

  {
    paint = new Paint(0);
    paint.setAntiAlias(true);
  }

  public CircleTextView(Context context) {
    super(context);
  }

  public CircleTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override protected void onDraw(Canvas canvas) {
    float size = getMeasuredWidth() / 2f;
    int alpha = paint.getAlpha();
    if (!isEnabled()) {
      paint.setAlpha(alpha / 2);
    }
    canvas.drawCircle(size, size, size, paint);
    paint.setAlpha(alpha);
    super.onDraw(canvas);
  }

  public void setCircleColor(int color) {
    paint.setColor(color);
  }
}
