package l.files.ui.operations.action;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.Path;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.operations.action.Clipboard.Action.CUT;

public final class CutAction extends ActionModeItem {

    private final Selection<Path, ?> selection;

    public CutAction(Selection<Path, ?> selection) {
        super(android.R.id.cut);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.cut, menu);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        Clipboard.INSTANCE.set(CUT, selection.keys());
        mode.finish();
    }

}
