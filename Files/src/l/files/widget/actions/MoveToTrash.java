package l.files.widget.actions;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import l.files.R;
import l.files.widget.MultiChoiceModeActionAdapter;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static l.files.trash.TrashService.TrashMover;
import static l.files.widget.ListViews.getCheckedItems;

public final class MoveToTrash extends MultiChoiceModeActionAdapter {

  private final ListView listView;
  private final TrashMover mover;

  public MoveToTrash(ListView listView, TrashMover mover) {
    this.listView = listView;
    this.mover = mover;
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuItem item = menu.add(NONE, getItemId(), NONE, R.string.move_to_trash);
    item.setShowAsAction(SHOW_AS_ACTION_NEVER);
    return true;
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    mover.moveToTrash(getCheckedFiles());
    mode.finish();
    return true;
  }

  private Iterable<File> getCheckedFiles() {
    return getCheckedItems(listView, File.class);
  }

  @Override public int getItemId() {
    return R.id.move_to_trash;
  }
}