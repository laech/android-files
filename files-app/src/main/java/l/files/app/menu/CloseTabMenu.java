package l.files.app.menu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.ui.analytics.AnalyticsMenu;
import l.files.app.TabHandler;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Menu to close the current tab.
 */
public final class CloseTabMenu extends OptionsMenuAction {

  private final TabHandler handler;

  private CloseTabMenu(TabHandler handler) {
    super(R.id.close_tab);
    this.handler = checkNotNull(handler, "handler");
  }

  public static OptionsMenu create(Context context, TabHandler handler) {
    return new AnalyticsMenu(context, new CloseTabMenu(handler), "close_tab");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, R.string.close_tab)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override protected void onItemSelected(MenuItem item) {
    handler.closeCurrentTab();
  }
}
