package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;

import static android.widget.AbsListView.MultiChoiceModeListener;

public interface MultiChoiceMode {

  /**
   * @see MultiChoiceModeListener#onCreateActionMode(ActionMode, Menu)
   */
  void onCreate(ActionMode mode, Menu menu);

  /**
   * @see MultiChoiceModeListener#onItemCheckedStateChanged(ActionMode, int, long, boolean)
   */
  void onChange(ActionMode mode, int position, long id, boolean checked);
}
