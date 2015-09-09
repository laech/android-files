package l.files.common.widget;

import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;

final class CompositeItem implements Callback {

    private final Callback[] callbacks;

    CompositeItem(final Callback... callbacks) {
        this.callbacks = callbacks.clone();
    }

    @Override
    public boolean onCreateActionMode(
            final ActionMode mode,
            final Menu menu) {
        boolean create = true;
        for (final Callback callback : callbacks) {
            create &= callback.onCreateActionMode(mode, menu);
        }
        return create;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        boolean updated = false;
        for (final Callback callback : callbacks) {
            updated |= callback.onPrepareActionMode(mode, menu);
        }
        return updated;
    }

    @Override
    public boolean onActionItemClicked(
            final ActionMode mode,
            final MenuItem item) {
        for (final Callback callback : callbacks) {
            if (callback.onActionItemClicked(mode, item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        for (final Callback callback : callbacks) {
            callback.onDestroyActionMode(mode);
        }
    }
}
