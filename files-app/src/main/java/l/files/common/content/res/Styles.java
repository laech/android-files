package l.files.common.content.res;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;

/**
 * Utility for styled attributes such as those in {@link android.R.attr}.
 */
public final class Styles {
  private Styles() {
  }

  private static final int UNDEFINED = -1;

  public static int getResourceId(final int attr, final Context context) {
    final TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
    final int id = ta.getResourceId(0, UNDEFINED);
    ta.recycle();
    if (id == UNDEFINED) {
      throw new IllegalArgumentException();
    }
    return id;
  }

  public static ColorStateList getColorStateList(final int attr, final Context context) {
    final TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
    final ColorStateList result = ta.getColorStateList(0);
    ta.recycle();
    if (result == null) {
      throw new IllegalArgumentException();
    }
    return result;
  }

}
