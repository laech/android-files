package l.files.ui.app.files;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import l.files.R;
import l.files.ui.action.MultiChoiceModeActionAdapter;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.trash.TrashService.TrashMover;
import static l.files.ui.util.ListViews.getCheckedItems;

public final class MoveToTrashAction extends MultiChoiceModeActionAdapter {

  private final AbsListView list;
  private final TrashMover mover;

  public MoveToTrashAction(AbsListView list, TrashMover mover) {
    this.list = checkNotNull(list, "list");
    this.mover = checkNotNull(mover, "mover");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuItem item = menu.add(NONE, getItemId(), NONE, R.string.move_to_trash);
    item.setShowAsAction(SHOW_AS_ACTION_NEVER);
    return true;
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    for (File file : getCheckedFiles()) {
      mover.moveToTrash(file);
    }
    mode.finish();
    return true;
  }

  private Iterable<File> getCheckedFiles() {
    return getCheckedItems(list, File.class);
  }

  @Override public int getItemId() {
    return R.id.move_to_trash;
  }
}