package l.files.app.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.widget.AbsListView;
import l.files.R;
import l.files.common.widget.MultiChoiceModeListenerAdapter;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CountSelectedItemsAction extends MultiChoiceModeListenerAdapter {

  private final AbsListView list;

  public CountSelectedItemsAction(AbsListView list) {
    this.list = checkNotNull(list, "list");
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
    mode.setTitle(list.getResources().getString(R.string.n_selected, n));
  }
}
