package l.files.app.mode;

import android.app.FragmentManager;
import android.database.Cursor;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import l.files.R;
import l.files.analytics.AnalyticsAction;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.provider.FilesContract;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItemPosition;
import static l.files.provider.FileCursors.getLocation;

/**
 * Lets the user rename a selected file in the list view, to be renamed to the
 * directory identified by the given {@link FilesContract.Files#LOCATION}.
 */
public final class RenameAction extends MultiChoiceModeAction {

  private final AbsListView list;
  private final FragmentManager manager;
  private final String parentLocation;

  private RenameAction(
      AbsListView list, FragmentManager manager, String parentLocation) {
    super(R.id.rename);
    this.list = checkNotNull(list, "list");
    this.manager = checkNotNull(manager, "manager");
    this.parentLocation = checkNotNull(parentLocation, "parentLocation");
  }

  public static MultiChoiceModeListener create(
      AbsListView list, FragmentManager manager, String parentLocation) {
    MultiChoiceModeListener action = new RenameAction(list, manager, parentLocation);
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
    String location = getLocation(cursor);
    RenameFragment.create(parentLocation, location).show(manager, RenameFragment.TAG);
  }
}
