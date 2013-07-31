package l.files.common.app;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class BaseFragmentActivity extends FragmentActivity {

  private OptionsMenu optionsMenu = OptionsMenus.EMPTY;

  public final void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = OptionsMenus.nullToEmpty(menu);
  }

  @Override public final boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    optionsMenu.onCreate(menu);
    return true;
  }

  @Override public final boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    optionsMenu.onPrepare(menu);
    return true;
  }

  @Override public final void onOptionsMenuClosed(Menu menu) {
    super.onOptionsMenuClosed(menu);
    optionsMenu.onClose(menu);
  }
}
