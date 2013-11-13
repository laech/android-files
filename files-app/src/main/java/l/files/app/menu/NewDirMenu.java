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

final class NewDirMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private final String parentId;
  private final FragmentManager manager;

  NewDirMenu(FragmentManager manager, String parentId) {
    this.manager = checkNotNull(manager, "manager");
    this.parentId = checkNotNull(parentId, "parentId");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.new_dir, NONE, R.string.new_dir)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    NewDirFragment.create(parentId).show(manager, NewDirFragment.TAG);
    return true;
  }
}
