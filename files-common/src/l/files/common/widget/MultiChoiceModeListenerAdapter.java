package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;

final class MultiChoiceModeListenerAdapter implements MultiChoiceModeListener {

  private final MultiChoiceMode mode;

  MultiChoiceModeListenerAdapter(MultiChoiceMode mode) {
    this.mode = checkNotNull(mode, "mode");
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode actionMode, int i, long l, boolean b) {
    mode.onChange(actionMode, i, l, b);
  }

  @Override public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
    mode.onCreate(actionMode, menu);
    return true;
  }

  @Override public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
    return false;
  }

  @Override public boolean onActionItemClicked(
      ActionMode actionMode, MenuItem menuItem) {
    return true;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {}
}
