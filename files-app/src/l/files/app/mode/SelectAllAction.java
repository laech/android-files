package l.files.app.mode;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import l.files.R;
import l.files.common.widget.SingleAction;

final class SelectAllAction extends SingleAction {

  private final AbsListView list;

  SelectAllAction(AbsListView list) {
    this.list = checkNotNull(list, "list");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, android.R.id.selectAll, NONE, android.R.string.selectAll)
        .setIcon(R.drawable.ic_action_select_all)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override protected int id() {
    return android.R.id.selectAll;
  }

  @Override protected void handleActionItemClicked(ActionMode mode, MenuItem item) {
    for (int i = 0; i < list.getCount(); ++i) {
      list.setItemChecked(i, true);
    }
  }
}
