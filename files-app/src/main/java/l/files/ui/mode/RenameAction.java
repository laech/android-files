package l.files.ui.mode;

import android.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Resource;
import l.files.ui.ListSelection;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

public final class RenameAction extends MultiChoiceModeAction {

    private final ListSelection<Resource> provider;
    private final FragmentManager manager;

    @SuppressWarnings("unchecked")
    public RenameAction(FragmentManager manager, ListSelection<? extends Resource> provider) {
        super(R.id.rename);
        this.manager = requireNonNull(manager, "manager");
        this.provider = (ListSelection<Resource>) requireNonNull(provider, "provider");
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.add(NONE, id(), NONE, R.string.rename)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public void onItemCheckedStateChanged(
            ActionMode mode, int position, long id, boolean checked) {
        MenuItem item = mode.getMenu().findItem(R.id.rename);
        if (item != null) {
            item.setEnabled(provider.getCheckedItemCount() == 1);
        }
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        Resource resource = provider.getCheckedItem();
        RenameFragment.create(resource).show(manager, RenameFragment.TAG);
    }
}
