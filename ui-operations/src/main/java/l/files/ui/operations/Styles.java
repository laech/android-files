package l.files.ui.operations;

import android.content.Context;
import android.content.res.TypedArray;

final class Styles {

    private Styles() {
    }

    private static final int UNDEFINED = -1;

    static int getResourceId(final int attr, final Context context) {
        final TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        final int id = ta.getResourceId(0, UNDEFINED);
        ta.recycle();
        if (id == UNDEFINED) {
            throw new IllegalArgumentException();
        }
        return id;
    }

}
