package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import l.files.R;
import l.files.common.widget.MultiChoiceModeAction;

import static java.util.Objects.requireNonNull;

public final class SelectAllAction extends MultiChoiceModeAction
{
    private final AbsListView list;

    public SelectAllAction(final AbsListView list)
    {
        super(android.R.id.selectAll);
        this.list = requireNonNull(list, "list");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu)
    {
        mode.getMenuInflater().inflate(R.menu.select_all, menu);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item)
    {
        final int count = list.getCount();
        for (int i = count - 1; i >= 0; --i)
        {
            list.setItemChecked(i, true);
        }
    }
}
