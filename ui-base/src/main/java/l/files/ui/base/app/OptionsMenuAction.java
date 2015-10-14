package l.files.ui.base.app;

import android.view.MenuItem;

/**
 * An {@link OptionsMenu} that contains only a single menu item.
 */
public abstract class OptionsMenuAction extends OptionsMenuAdapter {

    private final int id;

    public OptionsMenuAction(int id) {
        this.id = id;
    }

    /**
     * The ID of this menu item.
     */
    protected final int id() {
        return id;
    }

    /**
     * Handles the click event of this menu item.
     */
    protected abstract void onItemSelected(MenuItem item);

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == id()) {
            onItemSelected(item);
            return true;
        }
        return false;
    }
}
