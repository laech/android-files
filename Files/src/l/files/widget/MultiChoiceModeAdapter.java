package l.files.widget;

import android.view.ActionMode;
import l.files.view.ActionModeAdapter;

import static android.widget.AbsListView.MultiChoiceModeListener;

public class MultiChoiceModeAdapter
    extends ActionModeAdapter implements MultiChoiceModeListener {

  @Override
  public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
  }

}
