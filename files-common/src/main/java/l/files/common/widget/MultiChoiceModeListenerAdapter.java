package l.files.common.widget;

import static android.widget.AbsListView.MultiChoiceModeListener;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class MultiChoiceModeListenerAdapter implements MultiChoiceModeListener {

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    return true;
  }

  @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    return false;
  }

  @Override public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {}

  @Override public void onDestroyActionMode(ActionMode mode) {}
}
