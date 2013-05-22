package l.files.ui.menu;

import android.view.Menu;
import android.view.MenuItem;

/**
 * An action for a single options menu item.
 * {@link #onOptionsItemSelected(MenuItem)} will only be called if the ID of the
 * menu item is the same as {@link #getItemId()}.
 */
public interface OptionsMenuAction {

  /**
   * Returns the item ID of this option menu action, or 0 if this action has no
   * visual item.
   */
  int getItemId();

  void onCreateOptionsMenu(Menu menu);

  void onPrepareOptionsMenu(Menu menu);

  void onOptionsItemSelected(MenuItem item);

}
