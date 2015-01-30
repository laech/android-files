package l.files.ui.mode;

import android.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import l.files.R;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.FileStatus;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItemPosition;

public final class RenameAction extends MultiChoiceModeAction {

  private final AbsListView list;
  private final FragmentManager manager;

  public RenameAction(FragmentManager manager, AbsListView list) {
    super(R.id.rename);
    this.manager = checkNotNull(manager);
    this.list = checkNotNull(list);
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
      item.setEnabled(list.getCheckedItemCount() == 1);
    }
  }

  @Override protected void onItemSelected(ActionMode mode, MenuItem item) {
    int position = getCheckedItemPosition(list);
    FileStatus stat = (FileStatus) list.getItemAtPosition(position);
    RenameFragment.create(stat.path()).show(manager, RenameFragment.TAG);
  }
}
