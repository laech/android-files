package l.files.common.app;

import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseListFragment extends ListFragment {

  private OptionsMenu optionsMenu = OptionsMenus.EMPTY;

  public final void setOptionsMenu(OptionsMenu menu) {
    optionsMenu = OptionsMenus.nullToEmpty(menu);
  }

  @Override
  public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    optionsMenu.onCreateOptionsMenu(menu);
  }

  @Override public final void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    optionsMenu.onPrepareOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    return optionsMenu.onOptionsItemSelected(item);
  }
}
