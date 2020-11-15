package l.files.ui.browser.action;

import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.FragmentManager;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.browser.R;

import java.nio.file.Path;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.base.Objects.requireNonNull;

public final class RenameAction extends ActionModeItem
    implements Selection.Callback {

    private final Selection<Path, ?> selection;
    private final FragmentManager manager;

    public RenameAction(
        Selection<Path, ?> selection,
        FragmentManager manager
    ) {
        super(R.id.rename);
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
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        Path file = selection.keys().iterator().next();
        RenameFragment.create(file).show(manager, RenameFragment.TAG);
    }
}
