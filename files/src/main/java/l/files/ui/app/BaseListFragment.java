package l.files.ui.app;

import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import l.files.ui.menu.OptionsMenu;

public class BaseListFragment extends ListFragment {

  private OptionsMenu optionsMenu;

  public void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = OptionsMenu.nullToEmpty(menu);
    setHasOptionsMenu(!optionsMenu.isEmpty());
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    optionsMenu.onCreateOptionsMenu(menu);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    optionsMenu.onPrepareOptionsMenu(menu);
  }

}
