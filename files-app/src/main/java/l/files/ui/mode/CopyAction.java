package l.files.ui.mode;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Resource;
import l.files.ui.Clipboards;
import l.files.ui.ListSelection;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static java.util.Objects.requireNonNull;
import static l.files.common.content.res.Styles.getDrawable;

public final class CopyAction extends MultiChoiceModeAction {

    private final Context context;
    private final ClipboardManager manager;
    private final ListSelection<Resource> provider;

    @SuppressWarnings("unchecked")
    public CopyAction(Context context, ClipboardManager manager,
                      ListSelection<? extends Resource> provider) {
        super(android.R.id.copy);
        this.context = requireNonNull(context, "context");
        this.manager = requireNonNull(manager, "manager");
        this.provider = (ListSelection<Resource>) requireNonNull(provider, "provider");
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.add(NONE, id(), NONE, android.R.string.copy)
                .setIcon(getDrawable(android.R.attr.actionModeCopyDrawable, context))
                .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        Clipboards.setCopy(manager, provider.getCheckedItems());
        mode.finish();
    }
}
