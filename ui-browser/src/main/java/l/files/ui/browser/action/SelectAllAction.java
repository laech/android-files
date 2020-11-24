package l.files.ui.browser.action;

import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.view.ActionMode;
import l.files.ui.base.view.ActionModeItem;
import l.files.ui.browser.R;

import static java.util.Objects.requireNonNull;

public final class SelectAllAction extends ActionModeItem {

    private final Selectable selectable;

    public SelectAllAction(Selectable selectable) {
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
