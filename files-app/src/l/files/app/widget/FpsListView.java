package l.files.app.widget;

import static l.files.BuildConfig.DEBUG;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.ListView;

public final class FpsListView extends ListView {

  private long start = -1;
  private int counter;
  private int fps;

  private final Paint paint;

  public FpsListView(Context context, AttributeSet attrs) {
    super(context, attrs);

    paint = new Paint();
    paint.setColor(0xFF000000);
    paint.setAntiAlias(true);
    paint.setTextSize(paint.getTextSize() * 2);
  }

  @Override public void draw(Canvas canvas) {
    if (!DEBUG) {
      super.draw(canvas);
      return;
    }

    if (start == -1) {
      start = SystemClock.elapsedRealtime();
      counter = 0;
    }

    long now = SystemClock.elapsedRealtime();
    long delay = now - start;

    super.draw(canvas);

    canvas.drawText(fps + " fps", canvas.getWidth() - 80, 30, paint);

    if (delay > 1000l) {
      start = now;
      fps = counter;
      counter = 0;
    }

    counter++;
  }
}
