package l.files.app.mode;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import l.files.R;
import l.files.app.TrashService;
import l.files.common.widget.MultiChoiceActionAdapter;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItems;

final class DeleteAction
    extends MultiChoiceActionAdapter implements OnMenuItemClickListener {

  private final Context context;
  private final AbsListView list;
  private ActionMode mode;

  public DeleteAction(Context context, AbsListView list) {
    this.context = checkNotNull(context, "context");
    this.list = checkNotNull(list, "list");
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    this.mode = mode;
    menu.add(NONE, R.id.delete, NONE, R.string.delete)
        .setOnMenuItemClickListener(this)
        .setIcon(android.R.drawable.ic_menu_delete)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }

  private Iterable<File> getCheckedFiles() {
    return getCheckedItems(list, File.class);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    TrashService.delete(getCheckedFiles(), context);
    if (mode != null) {
      mode.finish();
    }
    return true;
  }
}