package l.files.ui.app;

import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import l.files.ui.menu.OptionsMenu;
import l.files.ui.menu.OptionsMenus;

public class BaseListFragment extends ListFragment {

  private OptionsMenu optionsMenu;

  public void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = OptionsMenus.nullToEmpty(menu);
    setHasOptionsMenu(menu != null);
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    optionsMenu.onCreate(menu);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    optionsMenu.onPrepare(menu);
  }

}
