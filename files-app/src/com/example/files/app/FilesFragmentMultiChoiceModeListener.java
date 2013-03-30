package com.example.files.app;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.example.files.widget.ListViews.removeCheckedItems;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import com.example.files.R;

final class FilesFragmentMultiChoiceModeListener
    implements MultiChoiceModeListener {

  private final FilesFragment fragment;

  FilesFragmentMultiChoiceModeListener(FilesFragment fragment) {
    this.fragment = fragment;
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    mode.getMenuInflater().inflate(R.menu.files_fragment_action_mode, menu);
    updateActionModeTitle(mode);
    return true;
  }

  @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override public void onItemCheckedStateChanged(
      ActionMode mode, int position, long id, boolean checked) {
    updateActionModeTitle(mode);
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.move_to_trash:
        return moveCheckedItemsToTrash(mode);
    }
    return false;
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
  }

  private boolean moveCheckedItemsToTrash(ActionMode mode) {
    removeCheckedItems(fragment.getListView(), fragment.getListAdapter());
    mode.finish();
    return true;
  }

  private void updateActionModeTitle(ActionMode mode) {
    int n = fragment.getListView().getCheckedItemCount();
    mode.setTitle(fragment.getString(R.string.n_selected, n));
  }
}
