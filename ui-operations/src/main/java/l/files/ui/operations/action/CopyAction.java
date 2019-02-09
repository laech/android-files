package l.files.ui.operations.action;

import androidx.appcompat.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.Path;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.operations.action.Clipboard.Action.COPY;

public final class CopyAction extends ActionModeItem {

    private final Selection<Path, ?> selection;

    public CopyAction(Selection<Path, ?> selection) {
        super(android.R.id.copy);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.copy, menu);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        Clipboard.INSTANCE.set(COPY, selection.keys());
        mode.finish();
    }

}
