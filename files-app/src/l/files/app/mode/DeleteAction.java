package l.files.app.mode;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItems;

import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import java.io.File;
import java.util.List;
import l.files.R;
import l.files.common.widget.MultiChoiceActionAdapter;

final class DeleteAction
    extends MultiChoiceActionAdapter implements OnMenuItemClickListener {

  private final FragmentManager manager;
  private final AbsListView list;

  public DeleteAction(AbsListView list, FragmentManager manager) {
    this.manager = checkNotNull(manager, "manager");
    this.list = checkNotNull(list, "list");
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    menu.add(NONE, R.id.delete, NONE, R.string.delete)
        .setOnMenuItemClickListener(this)
        .setIcon(R.drawable.ic_menu_delete)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    DeleteDialog.create(getCheckedFiles()).show(manager, DeleteDialog.FRAGMENT_TAG);
    return true;
  }

  private File[] getCheckedFiles() {
    List<File> files = getCheckedItems(list, File.class);
    return files.toArray(new File[files.size()]);
  }
}