package l.files.ui.browser;

import android.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import l.files.ui.R;
import l.files.ui.base.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

final class SortMenu extends OptionsMenuAction {

    private final FragmentManager manager;

    SortMenu(FragmentManager manager) {
        super(R.id.sort_by);
        this.manager = requireNonNull(manager);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, R.string.sort_by)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        new SortDialog().show(manager, SortDialog.FRAGMENT_TAG);
    }
}
