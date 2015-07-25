package l.files.ui.browser;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import static l.files.R.color.activated_highlight;

public final class ColorOverlayLinearLayout extends LinearLayout {

  private int overlayColor;

  {
    overlayColor = getContext().getResources().getColor(activated_highlight);
  }

  public ColorOverlayLinearLayout(Context context) {
    super(context);
  }

  public ColorOverlayLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ColorOverlayLinearLayout(
      Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ColorOverlayLinearLayout(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);
    if (isActivated()) {
      canvas.drawColor(overlayColor);
    }
  }

}
