package l.files.ui.app.files.menu;

import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.base.Supplier;
import l.files.R;
import l.files.ui.menu.OptionsMenuActionAdapter;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

public final class SortByAction
    extends OptionsMenuActionAdapter implements OnMenuItemClickListener {

  private final FragmentManager manager;
  private final Supplier<SortByDialog> dialog;

  public SortByAction(FragmentManager manager, Supplier<SortByDialog> dialog) {
    this.manager = checkNotNull(manager, "manager");
    this.dialog = checkNotNull(dialog, "dialog");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    MenuItem item = menu.add(NONE, R.id.sort_by, NONE, R.string.sort_by);
    item.setShowAsAction(SHOW_AS_ACTION_NEVER);
    item.setOnMenuItemClickListener(this);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    dialog.get().show(manager, null);
    return true;
  }
}
