package l.files.ui.mode;

import android.app.FragmentManager;
import android.database.Cursor;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import l.files.R;
import l.files.ui.analytics.AnalyticsAction;
import l.files.common.widget.MultiChoiceModeAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItemPosition;
import static l.files.provider.FilesContract.Files;

/**
 * Lets the user rename a selected file in the list view, to be renamed to the
 * directory identified by the given {@link Files#ID}.
 */
public final class RenameAction extends MultiChoiceModeAction {

  private final AbsListView list;
  private final FragmentManager manager;
  private final String parentId;

  private RenameAction(
      AbsListView list, FragmentManager manager, String parentId) {
    super(R.id.rename);
    this.list = checkNotNull(list, "list");
    this.manager = checkNotNull(manager, "manager");
    this.parentId = checkNotNull(parentId, "parentId");
  }

  public static MultiChoiceModeListener create(
      AbsListView list, FragmentManager manager, String parentId) {
    MultiChoiceModeListener action = new RenameAction(list, manager, parentId);
    return new AnalyticsAction(list.getContext(), action, "rename");
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
    Cursor cursor = (Cursor) list.getItemAtPosition(position);
    String id = Files.id(cursor);
    RenameFragment.create(parentId, id).show(manager, RenameFragment.TAG);
  }
}
