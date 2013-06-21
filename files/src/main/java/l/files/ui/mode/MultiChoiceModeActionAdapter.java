package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class MultiChoiceModeActionAdapter implements MultiChoiceModeAction {

  @Override public int getItemId() {
    return 0;
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    return true;
  }

  @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    return false;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
  }
}
