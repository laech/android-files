package l.files.ui.app.files;

import android.view.Menu;
import android.view.MenuItem;
import l.files.R;
import l.files.ui.menu.OptionsMenuActionAdapter;

import java.io.File;
import java.util.Random;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

public final class NewDirectoryAction
    extends OptionsMenuActionAdapter implements OnMenuItemClickListener {

  private final File parent;

  public NewDirectoryAction(File parent) {
    this.parent = checkNotNull(parent, "parent");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.new_dir, NONE, R.string.new_dir)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    new File(parent, new Random().nextInt() + "").mkdir(); // TODO
    return true;
  }
}
