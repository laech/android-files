package l.files.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import static l.files.common.content.res.Styles.getColor;

public class CircleTextView extends TextView {

  private final Paint paint;

  {
    paint = new Paint(0);
    paint.setColor(getColor(android.R.attr.textColorTertiary, getContext()));
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
    canvas.drawCircle(size, size, size, paint);
    super.onDraw(canvas);
  }
}
