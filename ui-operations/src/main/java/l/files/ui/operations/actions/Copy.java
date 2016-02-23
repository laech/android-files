package l.files.ui.operations.actions;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import static l.files.base.Objects.requireNonNull;
import static l.files.ui.operations.actions.Clipboard.Action.COPY;

public final class Copy extends ActionModeItem {

    private final Selection<Name, ?> selection;
    private final Path directory;

    public Copy(Path directory, Selection<Name, ?> selection) {
        super(android.R.id.copy);
        this.directory = requireNonNull(directory);
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
        Clipboard.INSTANCE.set(COPY, directory, selection.keys());
        mode.finish();
    }

}
