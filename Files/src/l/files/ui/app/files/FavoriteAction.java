package l.files.ui.app.files;

import android.view.Menu;
import android.view.MenuItem;
import l.files.R;
import l.files.Settings;
import l.files.ui.menu.OptionsMenuActionAdapter;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class FavoriteAction
    extends OptionsMenuActionAdapter implements OnMenuItemClickListener {

  private final Settings settings;
  private final File file;

  public FavoriteAction(File file, Settings settings) {
    this.settings = checkNotNull(settings, "settings");
    this.file = checkNotNull(file, "file");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    MenuItem item = menu.add(NONE, R.id.favorite, NONE, R.string.favorite);
    item.setOnMenuItemClickListener(this);
    item.setCheckable(true);
    item.setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepare(Menu menu) {
    super.onPrepare(menu);
    MenuItem item = menu.findItem(R.id.favorite);
    if (item != null) item.setChecked(settings.isFavorite(file));
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    settings.setFavorite(file, !item.isChecked());
    return true;
  }
}
