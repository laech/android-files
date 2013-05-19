package l.files.ui.action;

import android.view.ActionMode;
import android.view.Menu;
import android.widget.AbsListView;
import l.files.R;

import static com.google.common.base.Preconditions.checkNotNull;

public final class UpdateSelectedItemCountAction
    extends MultiChoiceModeActionAdapter {

  private final AbsListView listView;

  public UpdateSelectedItemCountAction(AbsListView listView) {
    this.listView = checkNotNull(listView, "listView");
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
    int n = listView.getCheckedItemCount();
    mode.setTitle(listView.getResources().getString(R.string.n_selected, n));
  }

}
