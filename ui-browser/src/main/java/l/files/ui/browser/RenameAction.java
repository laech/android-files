package l.files.ui.browser;

import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.base.Objects.requireNonNull;

final class RenameAction extends ActionModeItem
        implements Selection.Callback {

    private final Selection<File> selection;
    private final FragmentManager manager;

    RenameAction(Selection<File> selection, FragmentManager manager) {
        super(R.id.rename);
        this.manager = requireNonNull(manager, "manager");
        this.selection = requireNonNull(selection, "selection");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        super.onCreateActionMode(mode, menu);
        menu.add(NONE, id(), NONE, R.string.rename)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        selection.addWeaklyReferencedCallback(this);
        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        super.onDestroyActionMode(mode);
        selection.removeCallback(this);
    }

    @Override
    public void onSelectionChanged() {
        if (mode == null) {
            return;
        }

        final MenuItem item = mode.getMenu().findItem(R.id.rename);
        if (item != null) {
            item.setEnabled(selection.size() == 1);
        }
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item) {
        final File file = selection.copy().iterator().next();
        RenameFragment.create(file).show(manager, RenameFragment.TAG);
    }
}
