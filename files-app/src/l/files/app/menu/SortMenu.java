package l.files.app.menu;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.base.Supplier;
import l.files.R;
import l.files.common.app.OptionsMenuAdapter;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class SortMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private final FragmentManager manager;
  private final Supplier<DialogFragment> dialog;

  @SuppressWarnings("unchecked")
  SortMenu(FragmentManager manager, Supplier<? extends DialogFragment> dialog) {
    this.manager = checkNotNull(manager, "manager");
    this.dialog = (Supplier<DialogFragment>) checkNotNull(dialog, "dialog");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.sort_by, NONE, R.string.sort_by)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    dialog.get().show(manager, null);
    return true;
  }
}
