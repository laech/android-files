package l.files.widget.actions;

import android.view.ActionMode;
import android.view.Menu;
import android.widget.ListView;
import l.files.R;
import l.files.widget.MultiChoiceModeActionAdapter;

import static com.google.common.base.Preconditions.checkNotNull;

public final class UpdateSelectedItemCount extends MultiChoiceModeActionAdapter {

  private final ListView listView;

  public UpdateSelectedItemCount(ListView listView) {
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
