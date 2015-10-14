package l.files.ui.base.app;

import android.app.Activity;
import android.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Represents a standalone implementation of the options menu of an activity or
 * fragment. This makes each menu implementation more reusable as they can
 * exists outside of an activity or fragment.
 */
public interface OptionsMenu {
    /**
     * @see Activity#onCreateOptionsMenu(Menu)
     * @see Fragment#onCreateOptionsMenu(Menu, MenuInflater)
     */
    void onCreateOptionsMenu(Menu menu);

    /**
     * @see Activity#onPrepareOptionsMenu(Menu)
     * @see Fragment#onPrepareOptionsMenu(Menu)
     */
    void onPrepareOptionsMenu(Menu menu);

    /**
     * @see Activity#onOptionsItemSelected(MenuItem)
     * @see Fragment#onOptionsItemSelected(MenuItem)
     */
    boolean onOptionsItemSelected(MenuItem item);
}
