package l.files.common.content.res;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Utility for styled attributes such as those in {@link android.R.attr}.
 */
public final class Styles {
  private Styles() {}

  private static final int UNDEFINED = -1;

  /**
   * Gets the resource ID assigned to the given attribute.
   */
  public static int getResourceId(int attr, Context context) {
    TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
    int id = ta.getResourceId(0, UNDEFINED);
    ta.recycle();
    if (id == UNDEFINED) {
      throw new AssertionError();
    }
    return id;
  }

  /**
   * Gets the drawable assigned to the given attribute.
   */
  public static Drawable getDrawable(int attr, Context context) {
    TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
    Drawable icon = ta.getDrawable(0);
    ta.recycle();
    return icon;
  }

  /**
   * Gets the drawable assigned to the given attribute.
   */
  public static Drawable getDrawable(int attr, View view) {
    return getDrawable(attr, view.getContext());
  }

}
