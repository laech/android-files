package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;

import l.files.common.widget.MultiChoiceModeListenerAdapter;
import l.files.ui.ListSelection;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CountSelectedItemsAction extends MultiChoiceModeListenerAdapter {

  private final ListSelection<?> selection;

  public CountSelectedItemsAction(ListSelection<?> selection) {
    this.selection = checkNotNull(selection);
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
    mode.setTitle(Integer.toString(selection.getCheckedItemCount()));
  }
}
