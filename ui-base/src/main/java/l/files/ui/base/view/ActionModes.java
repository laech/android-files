package l.files.ui.base.view;

import android.view.ActionMode.Callback;

public final class ActionModes {
    private ActionModes() {
    }

    public static CompositeItem compose(final Callback... callbacks) {
        return new CompositeItem(callbacks);
    }
}
