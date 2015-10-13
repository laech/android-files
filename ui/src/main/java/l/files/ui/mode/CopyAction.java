package l.files.ui.mode;

import android.content.ClipboardManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.ui.R;
import l.files.common.widget.ActionModeItem;
import l.files.fs.File;
import l.files.ui.Clipboards;
import l.files.ui.selection.Selection;

import static java.util.Objects.requireNonNull;

public final class CopyAction extends ActionModeItem {

    private final ClipboardManager manager;
    private final Selection<File> selection;

    public CopyAction(
            final ClipboardManager manager,
            final Selection<File> selection) {
        super(android.R.id.copy);
        this.manager = requireNonNull(manager, "manager");
        this.selection = requireNonNull(selection, "selection");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mode.getMenuInflater().inflate(R.menu.copy, menu);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item) {
        Clipboards.setCopy(manager, selection.copy());
        mode.finish();
    }
}
