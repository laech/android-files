package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import l.files.common.widget.MultiChoiceModeAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.content.res.Styles.getDrawable;

public final class SelectAllAction extends MultiChoiceModeAction {

  private final AbsListView list;

  public SelectAllAction(AbsListView list) {
    super(android.R.id.selectAll);
    this.list = checkNotNull(list, "list");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, android.R.string.selectAll)
        .setIcon(getDrawable(android.R.attr.actionModeSelectAllDrawable, list))
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
    return true;
  }

  @Override protected void onItemSelected(ActionMode mode, MenuItem item) {
    int count = list.getCount();
    for (int i = count - 1; i >= 0; --i) {
      list.setItemChecked(i, true);
    }
  }
}
