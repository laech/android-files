package l.files.common.view;

import android.view.ActionMode;

import javax.annotation.Nullable;

public interface ActionModeProvider
{
    @Nullable
    ActionMode currentActionMode();

    ActionMode startActionMode(ActionMode.Callback callback);
}
