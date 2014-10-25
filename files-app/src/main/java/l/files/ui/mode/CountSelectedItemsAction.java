package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.widget.AbsListView;

import l.files.common.widget.MultiChoiceModeListenerAdapter;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Counts how many items are selected and displays that as the action mode
 * title.
 */
public final class CountSelectedItemsAction extends MultiChoiceModeListenerAdapter {

  private final AbsListView list;

  private CountSelectedItemsAction(AbsListView list) {
    this.list = checkNotNull(list, "list");
  }

  public static MultiChoiceModeListener create(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    updateSelectedItemCount(mode);
    return true;
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
    updateSelectedItemCount(mode);
  }

  private void updateSelectedItemCount(ActionMode mode) {
    int n = list.getCheckedItemCount();
    mode.setTitle(String.valueOf(n));
  }
}
