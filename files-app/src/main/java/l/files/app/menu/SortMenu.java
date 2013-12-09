package l.files.app.menu;

import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

public final class SortMenu extends OptionsMenuAction {

  private final FragmentManager manager;

  public SortMenu(FragmentManager manager) {
    super(R.id.sort_by);
    this.manager = checkNotNull(manager, "manager");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, R.string.sort_by)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override protected void onItemSelected(MenuItem item) {
    new SortDialog().show(manager, SortDialog.FRAGMENT_TAG);
  }
}
