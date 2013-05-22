package l.files.ui.menu;

import android.view.Menu;
import android.view.MenuItem;

public final class OptionsMenu {

  private final OptionsMenuAction[] actions;

  public OptionsMenu(OptionsMenuAction... actions) {
    this.actions = actions;
  }

  public void onCreateOptionsMenu(Menu menu) {
    for (OptionsMenuAction action : actions)
      action.onCreateOptionsMenu(menu);
  }

  public void onPrepareOptionsMenu(Menu menu) {
    for (OptionsMenuAction action : actions)
      action.onPrepareOptionsMenu(menu);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    for (OptionsMenuAction action : actions) {
      if (action.getItemId() != 0 && action.getItemId() == item.getItemId()) {
        action.onOptionsItemSelected(item);
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return actions.length == 0;
  }
}
