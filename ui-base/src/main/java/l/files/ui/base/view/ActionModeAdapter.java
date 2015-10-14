package l.files.ui.base.view;

import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class ActionModeAdapter implements ActionMode.Callback {

    @Nullable
    protected ActionMode mode;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.mode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.mode = null;
    }
}
