package l.files.ui.app;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import l.files.ui.menu.OptionsMenu;
import l.files.ui.menu.OptionsMenus;

public class BaseFragmentActivity extends FragmentActivity {

  private OptionsMenu optionsMenu;

  public void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = OptionsMenus.nullToEmpty(menu);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    optionsMenu.onCreate(menu);
    return true;
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    optionsMenu.onPrepare(menu);
    return true;
  }

}
