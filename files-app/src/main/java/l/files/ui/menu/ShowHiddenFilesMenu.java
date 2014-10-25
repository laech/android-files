package l.files.ui.menu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.ui.analytics.AnalyticsMenu;
import l.files.ui.Preferences;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Menu to show/hide hidden files.
 */
public final class ShowHiddenFilesMenu extends OptionsMenuAction {

  private final Context context;

  private ShowHiddenFilesMenu(Context context) {
    super(R.id.show_hidden_files);
    this.context = checkNotNull(context, "context");
  }

  public static OptionsMenu create(Context context) {
    OptionsMenu menu = new ShowHiddenFilesMenu(context);
    return new AnalyticsMenu(context, menu, "show_hidden_files");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, R.string.show_hidden_files)
        .setCheckable(true)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem item = menu.findItem(R.id.show_hidden_files);
    if (item != null) {
      item.setChecked(Preferences.getShowHiddenFiles(context));
    }
  }

  @Override protected void onItemSelected(MenuItem item) {
    Preferences.setShowHiddenFiles(context, !item.isChecked());
  }
}
