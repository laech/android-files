package l.files.ui.app.files.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.widget.AbsListView;
import l.files.R;
import l.files.common.widget.MultiChoiceModeAdapter;

import static com.google.common.base.Preconditions.checkNotNull;

final class CountSelectedItemsAction extends MultiChoiceModeAdapter {

  private final AbsListView list;

  public CountSelectedItemsAction(AbsListView list) {
    this.list = checkNotNull(list, "list");
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    updateSelectedItemCount(mode);
  }

  @Override public void onChange(
      ActionMode mode, int position, long id, boolean checked) {
    updateSelectedItemCount(mode);
  }

  private void updateSelectedItemCount(ActionMode mode) {
    int n = list.getCheckedItemCount();
    mode.setTitle(list.getResources().getString(R.string.n_selected, n));
  }
}
