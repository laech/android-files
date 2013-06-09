package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import static android.widget.AbsListView.MultiChoiceModeListener;

public class MultiChoiceModeDelegate implements MultiChoiceModeListener {

  private final MultiChoiceModeAction[] actions;

  public MultiChoiceModeDelegate(MultiChoiceModeAction... actions) {
    this.actions = actions;
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    boolean result = true;
    for (MultiChoiceModeAction action : actions) {
      result &= action.onCreateActionMode(mode, menu);
    }
    return result;
  }

  @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    boolean result = false;
    for (MultiChoiceModeAction action : actions) {
      result |= action.onPrepareActionMode(mode, menu);
    }
    return result;
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    for (MultiChoiceModeAction action : actions) {
      if (action.getItemId() != 0 && action.getItemId() == item.getItemId()) {
        action.onActionItemClicked(mode, item);
        return true;
      }
    }
    return false;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
    for (MultiChoiceModeAction action : actions) {
      action.onDestroyActionMode(mode);
    }
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
    for (MultiChoiceModeAction action : actions) {
      action.onItemCheckedStateChanged(mode, position, id, checked);
    }
  }
}
