package l.files.ui.mode;

import android.content.ClipboardManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Resource;
import l.files.ui.Clipboards;
import l.files.ui.ListSelection;

import static java.util.Objects.requireNonNull;

public final class CopyAction extends MultiChoiceModeAction
{

    private final ClipboardManager manager;
    private final ListSelection<Resource> provider;

    @SuppressWarnings("unchecked")
    public CopyAction(
            final ClipboardManager manager,
            final ListSelection<? extends Resource> provider)
    {
        super(android.R.id.copy);
        this.manager = requireNonNull(manager, "manager");
        this.provider = (ListSelection<Resource>)
                requireNonNull(provider, "provider");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu)
    {
        mode.getMenuInflater().inflate(R.menu.copy, menu);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item)
    {
        Clipboards.setCopy(manager, provider.getCheckedItems());
        mode.finish();
    }
}
