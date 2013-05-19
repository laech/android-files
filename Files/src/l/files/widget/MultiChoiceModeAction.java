package l.files.widget;

import android.view.ActionMode;
import android.view.MenuItem;

import static android.widget.AbsListView.MultiChoiceModeListener;

/**
 * A {@link MultiChoiceModeAction} is a {@MultiChoiceModeListener} that handles
 * only a single action. {@link #onActionItemClicked(ActionMode, MenuItem)} will
 * only be called if {@link #getItemId()} matches the ID of the clicked item.
 */
public interface MultiChoiceModeAction extends MultiChoiceModeListener {

  /**
   * Returns the item ID of this action, or 0 if this action has no visual item.
   */
  int getItemId();

}
