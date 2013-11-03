package l.files.app.mode;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;

import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import java.io.File;
import l.files.R;
import l.files.common.widget.MultiChoiceModeListenerAdapter;

final class RenameAction extends MultiChoiceModeListenerAdapter {

  private final AbsListView list;
  private final FragmentManager manager;

  RenameAction(AbsListView list, FragmentManager manager) {
    this.list = checkNotNull(list, "list");
    this.manager = checkNotNull(manager, "manager");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, R.id.rename, NONE, R.string.rename)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
    MenuItem item = mode.getMenu().findItem(R.id.rename);
    if (item != null) {
      item.setEnabled(list.getCheckedItemCount() == 1);
    }
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    Object object = list.getItemAtPosition(list.getCheckedItemPositions().keyAt(0));
    RenameFragment.create((File) object).show(manager, RenameFragment.TAG);
    return true;
  }
}
