package l.files.ui.base.view;

import androidx.appcompat.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

final class CompositeItem implements ActionMode.Callback {

    private final ActionMode.Callback[] callbacks;

    CompositeItem(ActionMode.Callback... callbacks) {
        this.callbacks = callbacks.clone();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        boolean create = true;
        for (ActionMode.Callback callback : callbacks) {
            create &= callback.onCreateActionMode(mode, menu);
        }
        return create;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        boolean updated = false;
        for (ActionMode.Callback callback : callbacks) {
            updated |= callback.onPrepareActionMode(mode, menu);
        }
        return updated;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        for (ActionMode.Callback callback : callbacks) {
            if (callback.onActionItemClicked(mode, item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        for (ActionMode.Callback callback : callbacks) {
            callback.onDestroyActionMode(mode);
        }
    }
}
