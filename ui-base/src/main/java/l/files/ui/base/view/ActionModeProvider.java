package l.files.ui.base.view;

import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;

public interface ActionModeProvider {

    @Nullable
    ActionMode currentActionMode();

    ActionMode startSupportActionMode(ActionMode.Callback callback);

}
