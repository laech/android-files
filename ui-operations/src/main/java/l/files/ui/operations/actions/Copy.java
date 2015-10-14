package l.files.ui.operations.actions;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.operations.R;

import static android.content.Context.CLIPBOARD_SERVICE;
import static java.util.Objects.requireNonNull;

public final class Copy extends ActionModeItem {

    private final ClipboardManager manager;
    private final Selection<File> selection;

    public Copy(Selection<File> selection, Context context) {
        this(selection, (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE));
    }

    Copy(Selection<File> selection, ClipboardManager manager) {
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
