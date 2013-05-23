package l.files.ui.app.files;

import android.view.Menu;
import android.view.MenuItem;
import l.files.R;
import l.files.Settings;
import l.files.ui.menu.OptionsMenuActionAdapter;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class FavoriteAction extends OptionsMenuActionAdapter {

  private final Settings settings;
  private final File file;

  public FavoriteAction(File file, Settings settings) {
    this.settings = checkNotNull(settings, "settings");
    this.file = checkNotNull(file, "file");
  }

  @Override public int getItemId() {
    return R.id.favorite;
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuItem item = menu.add(NONE, getItemId(), NONE, R.string.favorite);
    item.setShowAsAction(SHOW_AS_ACTION_NEVER);
    item.setCheckable(true);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem item = menu.findItem(R.id.favorite);
    if (item != null) item.setChecked(settings.isFavorite(file));
  }

  @Override public void onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    settings.setFavorite(file, !item.isChecked());
  }

}
