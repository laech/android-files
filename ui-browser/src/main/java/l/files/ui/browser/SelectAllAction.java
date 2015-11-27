package l.files.ui.browser;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.ui.base.view.ActionModeItem;

import static l.files.base.Objects.requireNonNull;

final class SelectAllAction extends ActionModeItem {

    private final Selectable selectable;

    SelectAllAction(Selectable selectable) {
        super(android.R.id.selectAll);
        this.selectable = requireNonNull(selectable);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        mode.getMenuInflater().inflate(R.menu.select_all, menu);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        selectable.selectAll();
    }
}
