package l.files.ui.menu;

import android.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.app.OptionsMenuAction;
import l.files.fs.Path;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static java.util.Objects.requireNonNull;

public final class NewDirMenu extends OptionsMenuAction {

  private final Path path;
  private final FragmentManager manager;

  public NewDirMenu(FragmentManager manager, Path path) {
    super(R.id.new_dir);
    this.manager = requireNonNull(manager, "manager");
    this.path = requireNonNull(path, "path");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, R.string.new_dir)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override protected void onItemSelected(MenuItem item) {
    NewDirFragment.create(path).show(manager, NewDirFragment.TAG);
  }
}
