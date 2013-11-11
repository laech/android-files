package l.files.app.menu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.app.Preferences;
import l.files.common.app.OptionsMenuAdapter;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class ShowHiddenFilesMenu
    extends OptionsMenuAdapter implements MenuItem.OnMenuItemClickListener {

  private final Context context;

  ShowHiddenFilesMenu(Context context) {
    this.context = checkNotNull(context, "context");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.show_hidden_files, NONE, R.string.show_hidden_files)
        .setCheckable(true)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepare(Menu menu) {
    super.onPrepare(menu);
    MenuItem item = menu.findItem(R.id.show_hidden_files);
    if (item != null) {
      item.setChecked(Preferences.getShowHiddenFiles(context));
    }
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    Preferences.setShowHiddenFiles(context, !item.isChecked());
    return true;
  }
}
