package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.widget.ActionModeItem;

import static java.util.Objects.requireNonNull;

public final class SelectAllAction extends ActionModeItem {
    private final Selectable selectable;

    public SelectAllAction(final Selectable selectable) {
        super(android.R.id.selectAll);
        this.selectable = requireNonNull(selectable, "selectable");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.select_all, menu);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item) {
        selectable.selectAll();
    }
}
