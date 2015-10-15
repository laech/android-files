package l.files.ui.browser;

import android.view.Menu;
import android.view.MenuItem;

import l.files.common.base.Provider;
import l.files.ui.R;
import l.files.ui.base.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

final class Refresh extends OptionsMenuAction {

    private final Provider<Boolean> show;
    private final Runnable action;

    Refresh(Provider<Boolean> show, Runnable action) {
        super(R.id.refresh);
        this.show = requireNonNull(show);
        this.action = requireNonNull(action);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, R.string.refresh)
                .setShowAsAction(SHOW_AS_ACTION_NEVER);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(id());
        if (item != null) {
            boolean enabled = show.get();
            item.setEnabled(enabled);
            item.setVisible(enabled);
        }
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        action.run();
    }

}
