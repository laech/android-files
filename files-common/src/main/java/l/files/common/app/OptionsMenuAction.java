package l.files.common.app;

import android.view.MenuItem;

/**
 * An {@link OptionsMenu} that contains only a single menu item.
 */
public abstract class OptionsMenuAction extends OptionsMenuAdapter {

  /**
   * The ID of the menu item.
   */
  protected abstract int id();

  protected abstract void onItemSelected(MenuItem item);

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == id()) {
      onItemSelected(item);
      return true;
    }
    return false;
  }
}
