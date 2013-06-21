package l.files.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import l.files.ui.animation.HeightChangeable;

public final class SizeableTextView
    extends TextView implements HeightChangeable {

  private int height = -1;

  public SizeableTextView(Context context) {
    super(context);
  }

  public SizeableTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SizeableTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (height > -1) setMeasuredDimension(getMeasuredWidth(), height);
  }

  @Override public int getOriginalHeight() {
    return getMeasuredHeight();
  }

  @Override public void setHeight(int height) {
    this.height = height;
    requestLayout();
  }

  @Override public void resetHeight() {
    this.height = -1;
    requestLayout();
  }

}
