package l.files.ui.operations.actions;

import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.base.Objects.requireNonNull;

public final class Delete extends ActionModeItem {

    private final Selection<File,?> selection;
    private final FragmentManager manager;

    public Delete(Selection<File,?> selection, FragmentManager manager) {
        super(R.id.delete);
        this.manager = requireNonNull(manager);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        menu.add(NONE, id(), NONE, R.string.delete)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        new DeleteDialog(selection.keys(), mode)
                .show(manager, DeleteDialog.FRAGMENT_TAG);
    }

}
