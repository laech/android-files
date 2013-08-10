package l.files.app.menu;

import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import l.files.R;
import l.files.common.app.OptionsMenuAdapter;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class SortMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private final FragmentManager manager;

  @SuppressWarnings("unchecked") SortMenu(FragmentManager manager) {
    this.manager = checkNotNull(manager, "manager");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.sort_by, NONE, R.string.sort_by)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    new SortDialog().show(manager, SortDialog.FRAGMENT_TAG);
    return true;
  }
}
