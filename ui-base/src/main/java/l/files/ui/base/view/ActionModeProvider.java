package l.files.ui.base.view;

import android.support.annotation.Nullable;
import android.view.ActionMode;

public interface ActionModeProvider {

    @Nullable
    ActionMode currentActionMode();

    ActionMode startActionMode(ActionMode.Callback callback);

}