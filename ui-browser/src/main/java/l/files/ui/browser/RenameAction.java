package l.files.ui.browser;

import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.Path;
import l.files.premium.PremiumActionModeItem;
import l.files.premium.PremiumLock;
import l.files.ui.base.selection.Selection;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.base.Objects.requireNonNull;

final class RenameAction extends PremiumActionModeItem
        implements Selection.Callback {

    private final Selection<Path, ?> selection;
    private final FragmentManager manager;

    RenameAction(
            PremiumLock premiumLock,
            Selection<Path, ?> selection,
            FragmentManager manager
    ) {
        super(R.id.rename, premiumLock);
        this.manager = requireNonNull(manager, "manager");
        this.selection = requireNonNull(selection, "selection");
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
    protected void doOnItemSelected(ActionMode mode, MenuItem item) {
        Path file = selection.keys().iterator().next();
        RenameFragment.create(file).show(manager, RenameFragment.TAG);
    }
}
