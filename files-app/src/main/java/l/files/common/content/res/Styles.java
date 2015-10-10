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
