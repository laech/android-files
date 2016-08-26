package l.files.ui.base.view;

import android.support.annotation.CallSuper;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import javax.annotation.Nullable;

public class ActionModeAdapter implements ActionMode.Callback {

    @Nullable
    protected ActionMode mode;

    @Override
    @CallSuper
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
    @CallSuper
    public void onDestroyActionMode(ActionMode mode) {
        this.mode = null;
    }
}
