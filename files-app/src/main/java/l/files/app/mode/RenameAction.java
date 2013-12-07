package l.files.app.mode;

import android.database.Cursor;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import l.files.R;
import l.files.common.widget.MultiChoiceModeListenerAdapter;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItemPosition;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;

public final class RenameAction extends MultiChoiceModeListenerAdapter {

  private final AbsListView list;
  private final FragmentManager manager;
  private final String parentId;

  public RenameAction(AbsListView list, FragmentManager manager, String parentId) {
    this.list = checkNotNull(list, "list");
    this.manager = checkNotNull(manager, "manager");
    this.parentId = checkNotNull(parentId, "parentId");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, R.id.rename, NONE, R.string.rename)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
    MenuItem item = mode.getMenu().findItem(R.id.rename);
    if (item != null) item.setEnabled(list.getCheckedItemCount() == 1);
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    int position = getCheckedItemPosition(list);
    Cursor cursor = (Cursor) list.getItemAtPosition(position);
    String fileId = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
    RenameFragment.create(parentId, fileId).show(manager, RenameFragment.TAG);
    return true;
  }
}
