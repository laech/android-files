package l.files.ui.app.files.menu;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import l.files.R;
import l.files.ui.app.settings.SettingsActivity;
import l.files.ui.menu.OptionsMenuActionAdapter;

import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

public final class SettingsAction extends OptionsMenuActionAdapter {

  private final Context context;

  public SettingsAction(Context context) {
    this.context = checkNotNull(context, "context");
  }

  @Override public void onCreate(Menu menu) {
    MenuItem item = menu.add(NONE, R.id.settings, CATEGORY_SECONDARY, R.string.settings);
    item.setIntent(new Intent(context, SettingsActivity.class));
    item.setShowAsAction(SHOW_AS_ACTION_NEVER);
  }
}
