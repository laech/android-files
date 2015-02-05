package l.files.ui.mode;

import android.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Path;
import l.files.ui.ListProvider;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;

public final class RenameAction extends MultiChoiceModeAction {

  private final ListProvider<Path> provider;
  private final FragmentManager manager;

  @SuppressWarnings("unchecked")
  public RenameAction(FragmentManager manager, ListProvider<? extends Path> provider) {
    super(R.id.rename);
    this.manager = checkNotNull(manager);
    this.provider = (ListProvider<Path>) checkNotNull(provider);
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, R.string.rename)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
    MenuItem item = mode.getMenu().findItem(R.id.rename);
    if (item != null) {
      item.setEnabled(provider.getCheckedItemCount() == 1);
    }
  }

  @Override protected void onItemSelected(ActionMode mode, MenuItem item) {
    Path path = provider.getCheckedItem();
    RenameFragment.create(path).show(manager, RenameFragment.TAG);
  }
}
