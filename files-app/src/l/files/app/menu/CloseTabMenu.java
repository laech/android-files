package l.files.app.menu;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

import android.view.Menu;
import android.view.MenuItem;
import l.files.R;
import l.files.app.TabHandler;
import l.files.common.app.OptionsMenuAdapter;

public final class CloseTabMenu extends OptionsMenuAdapter
    implements OnMenuItemClickListener {

  private final TabHandler handler;

  public CloseTabMenu(TabHandler handler) {
    this.handler = checkNotNull(handler, "handler");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.close_tab, NONE, R.string.close_tab)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    handler.closeCurrentTab();
    return true;
  }
}