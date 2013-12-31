package l.files.common.app;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {

  private OptionsMenu optionsMenu = OptionsMenus.EMPTY;

  public final void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = OptionsMenus.nullToEmpty(menu);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    optionsMenu.onCreateOptionsMenu(menu);
    return true;
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    optionsMenu.onPrepareOptionsMenu(menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    return optionsMenu.onOptionsItemSelected(item);
  }
}
