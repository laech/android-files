package l.files.ui.base.app;

import android.view.Menu;
import android.view.MenuItem;

public final class CompositeMenu implements OptionsMenu {

    private final OptionsMenu[] actions;

    CompositeMenu(OptionsMenu... actions) {
        this.actions = actions;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        for (OptionsMenu action : actions) {
            action.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        for (OptionsMenu action : actions) {
            action.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        for (OptionsMenu action : actions) {
            if (action.onOptionsItemSelected(item)) {
                return true;
            }
        }
        return false;
    }
}
