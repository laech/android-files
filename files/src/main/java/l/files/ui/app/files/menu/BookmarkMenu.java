package l.files.ui.app.files.menu;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import l.files.R;
import l.files.setting.SetSetting;
import l.files.ui.menu.OptionsMenuAdapter;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class BookmarkMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private final SetSetting<File> setting;
  private final File dir;

  BookmarkMenu(File dir, SetSetting<File> setting) {
    this.setting = checkNotNull(setting, "setting");
    this.dir = checkNotNull(dir, "dir");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.bookmark, NONE, R.string.bookmark)
        .setOnMenuItemClickListener(this)
        .setCheckable(true)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepare(Menu menu) {
    super.onPrepare(menu);
    MenuItem item = menu.findItem(R.id.bookmark);
    if (item != null) {
      item.setChecked(setting.contains(dir));
    }
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    if (!item.isChecked()) {
      setting.add(dir);
    } else {
      setting.remove(dir);
    }
    return true;
  }
}
