package l.files.ui.app.files.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import l.files.R;
import l.files.common.widget.MultiChoiceActionAdapter;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.trash.TrashService.TrashMover;
import static l.files.ui.util.ListViews.getCheckedItems;

final class MoveToTrashAction
    extends MultiChoiceActionAdapter implements OnMenuItemClickListener {

  private final AbsListView list;
  private final TrashMover mover;

  private ActionMode mode;

  public MoveToTrashAction(AbsListView list, TrashMover mover) {
    this.list = checkNotNull(list, "list");
    this.mover = checkNotNull(mover, "mover");
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    this.mode = mode;
    menu.add(NONE, R.id.move_to_trash, NONE, R.string.move_to_trash)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  private Iterable<File> getCheckedFiles() {
    return getCheckedItems(list, File.class);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    for (File file : getCheckedFiles()) {
      mover.moveToTrash(file);
    }
    if (mode != null) {
      mode.finish();
    }
    return true;
  }
}