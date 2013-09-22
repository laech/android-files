package l.files.app.menu;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

import android.view.Menu;
import android.view.MenuItem;
import l.files.R;
import l.files.app.TabOpener;
import l.files.common.app.OptionsMenuAdapter;

public final class NewTabMenu extends OptionsMenuAdapter implements MenuItem.OnMenuItemClickListener {

  private final TabOpener opener;

  public NewTabMenu(TabOpener opener) {
    this.opener = checkNotNull(opener, "opener");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.new_tab, NONE, "New tab (Temp)")
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    opener.openNewTab();
    return true;
  }
}
