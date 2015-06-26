package l.files.common.widget;

import android.view.ActionMode;
import android.view.MenuItem;

public abstract class ActionModeItem extends ActionModeAdapter
{
    private final int id;

    public ActionModeItem(final int id)
    {
        this.id = id;
    }

    /**
     * The ID of this action mode action.
     */
    protected final int id()
    {
        return id;
    }

    /**
     * Handles the click event of this action.
     */
    protected abstract void onItemSelected(ActionMode mode, MenuItem item);

    @Override
    public final boolean onActionItemClicked(
            final ActionMode mode,
            final MenuItem item)
    {
        if (item.getItemId() == id())
        {
            onItemSelected(mode, item);
            return true;
        }
        return false;
    }
}
