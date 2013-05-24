package l.files.ui.menu;

import android.view.Menu;

public final class OptionsMenu {

  private final OptionsMenuAction[] actions;

  public OptionsMenu(OptionsMenuAction... actions) {
    this.actions = actions;
  }

  public void onCreateOptionsMenu(Menu menu) {
    for (OptionsMenuAction action : actions) action.onCreate(menu);
  }

  public void onPrepareOptionsMenu(Menu menu) {
    for (OptionsMenuAction action : actions) action.onPrepare(menu);
  }

  public boolean isEmpty() {
    return actions.length == 0;
  }
}
