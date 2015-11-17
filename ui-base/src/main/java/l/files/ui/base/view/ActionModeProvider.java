package l.files.ui.base.view;

import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;

public interface ActionModeProvider {

    @Nullable
    ActionMode currentActionMode();

    ActionMode startSupportActionMode(ActionMode.Callback callback);

}
