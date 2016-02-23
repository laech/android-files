package l.files.ui.browser;

import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.base.Objects.requireNonNull;

final class RenameAction extends ActionModeItem
        implements Selection.Callback {

    private final Path sourceDirectory;
    private final Selection<Name, ?> selection;
    private final FragmentManager manager;

    RenameAction(
            Path sourceDirectory,
            Selection<Name, ?> selection,
            FragmentManager manager) {

        super(R.id.rename);

        this.sourceDirectory = requireNonNull(sourceDirectory);
        this.manager = requireNonNull(manager);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        menu.add(NONE, id(), NONE, R.string.rename)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        selection.addWeaklyReferencedCallback(this);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        super.onDestroyActionMode(mode);
        selection.removeCallback(this);
    }

    @Override
    public void onSelectionChanged() {
        if (mode == null) {
            return;
        }

        MenuItem item = mode.getMenu().findItem(R.id.rename);
        if (item != null) {
            item.setEnabled(selection.size() == 1);
        }
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        Name sourceFile = selection.keys().iterator().next();
        RenameFragment.create(sourceDirectory, sourceFile).show(manager, RenameFragment.TAG);
    }
}
