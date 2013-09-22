package l.files.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public final class ViewPager extends android.support.v4.view.ViewPager {

  public ViewPager(Context context) {
    super(context);
  }

  public ViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    return isEnabled() && super.onInterceptTouchEvent(ev);
  }
}
