package l.files.common.app;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {

  private OptionsMenu optionsMenu = OptionsMenus.EMPTY;
  private Menu menu;

  public Menu getMenu() {
    return menu;
  }

  public final void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = OptionsMenus.nullToEmpty(menu);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    this.optionsMenu.onCreateOptionsMenu(menu);
    this.menu = menu;
    return true;
  }

  @Override public void onOptionsMenuClosed(Menu menu) {
    super.onOptionsMenuClosed(menu);
    this.menu = null;
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    this.optionsMenu.onPrepareOptionsMenu(menu);
    this.menu = menu;
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    return optionsMenu.onOptionsItemSelected(item);
  }
}
