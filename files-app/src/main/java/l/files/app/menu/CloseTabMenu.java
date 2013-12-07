package l.files.app.menu;

import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.app.TabHandler;
import l.files.common.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

public final class CloseTabMenu extends OptionsMenuAction {

  private final TabHandler handler;

  public CloseTabMenu(TabHandler handler) {
    this.handler = checkNotNull(handler, "handler");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, R.string.close_tab)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override protected void onItemSelected(MenuItem item) {
    handler.closeCurrentTab();
  }

  @Override protected int id() {
    return R.id.close_tab;
  }
}
