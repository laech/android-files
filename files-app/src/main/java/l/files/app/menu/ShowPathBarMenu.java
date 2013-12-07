package l.files.app.menu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.app.OptionsMenuAdapter;

import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.app.Preferences.getShowPathBar;
import static l.files.app.Preferences.setShowPathBar;

public final class ShowPathBarMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private final Context context;

  public ShowPathBarMenu(Context context) {
    this.context = context;
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.show_path_bar, CATEGORY_SECONDARY, "Show path bar")
        .setCheckable(true)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepare(Menu menu) {
    super.onPrepare(menu);
    MenuItem item = menu.findItem(R.id.show_path_bar);
    if (item != null) {
      item.setChecked(getShowPathBar(context));
    }
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    setShowPathBar(context, !item.isChecked());
    return true;
  }
}
