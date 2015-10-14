package l.files.ui.base.app;

import android.view.Menu;
import android.view.MenuItem;

public final class CompositeMenu implements OptionsMenu {

    private final OptionsMenu[] actions;

    public CompositeMenu(final OptionsMenu... actions) {
        this.actions = actions;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu) {
        for (final OptionsMenu action : actions) {
            action.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        for (final OptionsMenu action : actions) {
            action.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        for (final OptionsMenu action : actions) {
            if (action.onOptionsItemSelected(item)) {
                return true;
            }
        }
        return false;
    }
}
