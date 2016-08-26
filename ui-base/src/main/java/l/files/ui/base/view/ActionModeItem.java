package l.files.ui.base.view;

import android.support.v7.view.ActionMode;
import android.view.MenuItem;

public abstract class ActionModeItem extends ActionModeAdapter {

    private final int id;

    public ActionModeItem(final int id) {
        this.id = id;
    }

    /**
     * The ID of this action mode action.
     */
    protected final int id() {
        return id;
    }

    /**
     * Handles the click event of this action.
     */
    protected abstract void onItemSelected(ActionMode mode, MenuItem item);

    @Override
    public final boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == id()) {
            onItemSelected(mode, item);
            return true;
        }
        return false;
    }
}
