package l.files.ui.base.view;

import androidx.appcompat.view.ActionMode;

public final class ActionModes {

    private ActionModes() {
    }

    public static ActionMode.Callback compose(ActionMode.Callback... callbacks) {
        return new CompositeItem(callbacks);
    }
}
