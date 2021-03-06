package l.files.ui.browser.menu;

import android.view.Menu;
import android.view.MenuItem;
import l.files.ui.base.app.OptionsMenuAction;
import l.files.ui.browser.R;

import java.util.function.BooleanSupplier;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

public final class RefreshMenu extends OptionsMenuAction {

    private final BooleanSupplier enable;
    private final Runnable action;

    public RefreshMenu(BooleanSupplier enable, Runnable action) {
        super(R.id.refresh);
        this.enable = requireNonNull(enable);
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
            item.setEnabled(enable.getAsBoolean());
        }
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        action.run();
    }

}
