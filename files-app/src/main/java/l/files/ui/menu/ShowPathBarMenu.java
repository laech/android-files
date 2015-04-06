package l.files.ui.menu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.app.OptionsMenuAction;
import l.files.ui.Preferences;

import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

public final class ShowPathBarMenu extends OptionsMenuAction {

  private final Context context;

  public ShowPathBarMenu(Context context) {
    super(R.id.show_path_bar);
    this.context = requireNonNull(context);
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), CATEGORY_SECONDARY, R.string.show_path_bar)
        .setCheckable(true)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem item = menu.findItem(R.id.show_path_bar);
    if (item != null) {
      item.setChecked(Preferences.getShowPathBar(context));
    }
  }

  @Override protected void onItemSelected(MenuItem item) {
    Preferences.setShowPathBar(context, !item.isChecked());
  }
}
