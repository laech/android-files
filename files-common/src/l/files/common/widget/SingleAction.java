package l.files.common.widget;

import android.view.ActionMode;
import android.view.MenuItem;

public abstract class SingleAction extends MultiChoiceModeListenerAdapter {

  protected abstract int id();

  protected abstract void handleActionItemClicked(ActionMode mode, MenuItem item);

  @Override public final boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    if (item.getItemId() == id()) {
      handleActionItemClicked(mode, item);
      return true;
    }
    return false;
  }
}
