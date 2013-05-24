package l.files.ui.app;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import l.files.ui.menu.OptionsMenu;

public class BaseFragmentActivity extends FragmentActivity {

  private OptionsMenu optionsMenu;

  public void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = menu != null ? menu : new OptionsMenu();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    optionsMenu.onCreateOptionsMenu(menu);
    return !optionsMenu.isEmpty();
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    optionsMenu.onPrepareOptionsMenu(menu);
    return !optionsMenu.isEmpty();
  }

}
