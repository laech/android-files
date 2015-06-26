package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import javax.annotation.Nullable;

public class ActionModeAdapter implements ActionMode.Callback
{
    @Nullable
    protected ActionMode mode;

    @Override
    public boolean onCreateActionMode(
            final ActionMode mode,
            final Menu menu)
    {
        this.mode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(
            final ActionMode mode,
            final Menu menu)
    {
        return false;
    }

    @Override
    public boolean onActionItemClicked(
            final ActionMode mode,
            final MenuItem item)
    {
        return false;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode)
    {
        this.mode = null;
    }
}
