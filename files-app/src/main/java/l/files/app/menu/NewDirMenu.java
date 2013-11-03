package l.files.app.menu;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import l.files.R;
import l.files.common.app.OptionsMenuAdapter;

final class NewDirMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private final File parent;
  private final FragmentManager manager;

  NewDirMenu(FragmentManager manager, File parent) {
    this.parent = checkNotNull(parent, "parent");
    this.manager = checkNotNull(manager, "manager");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.new_dir, NONE, R.string.new_dir)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    NewDirFragment.create(parent).show(manager, NewDirFragment.TAG);
    return true;
  }
}
