package l.files.ui.operations;

import android.content.Context;
import android.content.res.TypedArray;

final class Styles {

    private Styles() {
    }

    private static final int UNDEFINED = -1;

    static int getResourceId(int attr, Context context) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int id = ta.getResourceId(0, UNDEFINED);
        ta.recycle();
        if (id == UNDEFINED) {
            throw new IllegalArgumentException();
        }
        return id;
    }

}
