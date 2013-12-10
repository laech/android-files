package l.files.common.widget;

import android.view.ActionMode;
import android.view.MenuItem;

import static android.widget.AbsListView.MultiChoiceModeListener;

/**
 * A {@link MultiChoiceModeListener} that contains a single action.
 */
public abstract class MultiChoiceModeAction
    extends MultiChoiceModeListenerAdapter {

  private final int id;

  public MultiChoiceModeAction(int id) {
    this.id = id;
  }

  /**
   * The ID of this action mode action.
   */
  protected final int id() {
    return id;
  }

  /**
   * Handles the click event of this action.
   */
  protected abstract void onItemSelected(ActionMode mode, MenuItem item);

  @Override
  public final boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    if (item.getItemId() == id()) {
      onItemSelected(mode, item);
      return true;
    }
    return false;
  }
}
