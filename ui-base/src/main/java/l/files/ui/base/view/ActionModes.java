package l.files.ui.base.view;

import android.support.v7.view.ActionMode;

public final class ActionModes {

    private ActionModes() {
    }

    public static CompositeItem compose(ActionMode.Callback... callbacks) {
        return new CompositeItem(callbacks);
    }
}
