package l.files.common.app;

import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;

public class BaseListFragment extends ListFragment {

  private OptionsMenu optionsMenu = OptionsMenus.EMPTY;

  public final void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = OptionsMenus.nullToEmpty(menu);
    setHasOptionsMenu(menu != null);
  }

  @Override
  public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    optionsMenu.onCreate(menu);
  }

  @Override public final void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    optionsMenu.onPrepare(menu);
  }
}
