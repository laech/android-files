package l.files.ui.operations.actions;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.operations.actions.Clipboard.Action.CUT;

public final class Cut extends ActionModeItem {

    private final Selection<File> selection;

    public Cut(Selection<File> selection) {
        super(android.R.id.cut);
        this.selection = requireNonNull(selection, "provider");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.cut, menu);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item) {
        Clipboard.INSTANCE.set(CUT, selection.copy());
        mode.finish();
    }

}
