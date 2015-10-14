package l.files.ui.operations;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.fs.File;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;

import static android.content.Context.CLIPBOARD_SERVICE;
import static java.util.Objects.requireNonNull;

public final class CutAction extends ActionModeItem {

    private final ClipboardManager manager;
    private final Selection<File> selection;

    public CutAction(Selection<File> selection, Context context) {
        this(selection, (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE));
    }

    CutAction(Selection<File> selection, ClipboardManager manager) {
        super(android.R.id.cut);
        this.manager = requireNonNull(manager, "manager");
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
        Clipboards.setCut(manager, selection.copy());
        mode.finish();
    }
}
