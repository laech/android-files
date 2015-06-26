package l.files.ui.rename;

import android.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.widget.ActionModeItem;
import l.files.fs.Resource;
import l.files.ui.selection.Selection;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

public final class RenameAction extends ActionModeItem
        implements Selection.Callback
{

    private final Selection<Resource> selection;
    private final FragmentManager manager;

    public RenameAction(
            final FragmentManager manager,
            final Selection<Resource> selection)
    {
        super(R.id.rename);
        this.manager = requireNonNull(manager, "manager");
        this.selection = requireNonNull(selection, "selection");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu)
    {
        super.onCreateActionMode(mode, menu);
        menu.add(NONE, id(), NONE, R.string.rename)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        selection.addWeaklyReferencedCallback(this);
        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode)
    {
        super.onDestroyActionMode(mode);
        selection.removeCallback(this);
    }

    @Override
    public void onSelectionChanged()
    {
        if (mode == null)
        {
            return;
        }

        final MenuItem item = mode.getMenu().findItem(R.id.rename);
        if (item != null)
        {
            item.setEnabled(selection.size() == 1);
        }
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item)
    {
        final Resource resource = selection.copy().iterator().next();
        RenameFragment.create(resource).show(manager, RenameFragment.TAG);
    }
}
