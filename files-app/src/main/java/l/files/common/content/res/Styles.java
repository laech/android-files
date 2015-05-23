package l.files.common.content.res;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Utility for styled attributes such as those in {@link android.R.attr}.
 */
public final class Styles
{
    private Styles()
    {
    }

    private static final int UNDEFINED = -1;

    public static int getResourceId(final int attr, final Context context)
    {
        final TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        final int id = ta.getResourceId(0, UNDEFINED);
        ta.recycle();
        if (id == UNDEFINED)
        {
            throw new AssertionError();
        }
        return id;
    }

}
