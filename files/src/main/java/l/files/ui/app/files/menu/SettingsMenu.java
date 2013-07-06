package l.files.ui.app.files.menu;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import l.files.R;
import l.files.ui.app.settings.SettingsActivity;
import l.files.ui.menu.OptionsMenuAdapter;

import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class SettingsMenu extends OptionsMenuAdapter {

  private final Context context;

  SettingsMenu(Context context) {
    this.context = checkNotNull(context, "context");
  }

  @Override public void onCreate(Menu menu) {
    menu.add(NONE, R.id.settings, CATEGORY_SECONDARY, R.string.settings)
        .setIntent(new Intent(context, SettingsActivity.class))
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }
}
