package l.files.ui.mode;

import android.content.ClipboardManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.widget.ActionModeItem;
import l.files.fs.Resource;
import l.files.ui.Clipboards;
import l.files.ui.selection.Selection;

import static java.util.Objects.requireNonNull;

public final class CutAction extends ActionModeItem
{

    private final ClipboardManager manager;
    private final Selection<Resource> selection;

    public CutAction(
            final ClipboardManager manager,
            final Selection<Resource> selection)
    {
        super(android.R.id.cut);
        this.manager = requireNonNull(manager, "manager");
        this.selection = requireNonNull(selection, "provider");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu)
    {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.cut, menu);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item)
    {
        Clipboards.setCut(manager, selection.copy());
        mode.finish();
    }
}
