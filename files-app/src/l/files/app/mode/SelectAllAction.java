package l.files.app.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import l.files.R;
import l.files.common.widget.MultiChoiceActionAdapter;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static com.google.common.base.Preconditions.checkNotNull;

final class SelectAllAction
    extends MultiChoiceActionAdapter implements OnMenuItemClickListener {

  private final AbsListView list;

  SelectAllAction(AbsListView list) {
    this.list = checkNotNull(list, "list");
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    super.onCreate(mode, menu);
    menu.add(NONE, android.R.id.selectAll, NONE, android.R.string.selectAll)
        .setIcon(R.drawable.ic_menu_select_all)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }


  @Override public boolean onMenuItemClick(MenuItem item) {
    for (int i = 0; i < list.getCount(); ++i) {
      list.setItemChecked(i, true);
    }
    return true;
  }
}
