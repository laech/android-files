package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;

import static android.widget.AbsListView.MultiChoiceModeListener;

/**
 * A simplified version of {@link MultiChoiceModeListener}, each instance is
 * intended to be implemented as a single action, but multiple instances can be
 * composed into a single action via {@link MultiChoiceActions#compose(MultiChoiceAction...)}
 */
public interface MultiChoiceAction {
  /**
   * @see MultiChoiceModeListener#onCreateActionMode(ActionMode, Menu)
   */
  void onCreate(ActionMode mode, Menu menu);

  /**
   * @see MultiChoiceModeListener#onItemCheckedStateChanged(ActionMode, int, long, boolean)
   */
  void onChange(ActionMode mode, int position, long id, boolean checked);
}
