package l.files.ui.operations.actions;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.operations.actions.Clipboard.Action.COPY;

public final class Copy extends ActionModeItem {

    private final Selection<File> selection;

    public Copy(Selection<File> selection) {
        super(android.R.id.copy);
        this.selection = requireNonNull(selection);
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mode.getMenuInflater().inflate(R.menu.copy, menu);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item) {
        Clipboard.INSTANCE.set(COPY, selection.copy());
        mode.finish();
    }

}
