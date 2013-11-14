package l.files.common.widget;

import static android.widget.AbsListView.MultiChoiceModeListener;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.collect.ImmutableList;
import java.util.List;

final class CompositeMultiChoiceAction implements MultiChoiceModeListener {

  private final List<MultiChoiceModeListener> listeners;

  CompositeMultiChoiceAction(MultiChoiceModeListener... listeners) {
    this.listeners = ImmutableList.copyOf(listeners);
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    boolean create = true;
    for (MultiChoiceModeListener listener : listeners) {
      create &= listener.onCreateActionMode(mode, menu);
    }
    return create;
  }

  @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    boolean updated = false;
    for (MultiChoiceModeListener listener : listeners) {
      updated |= listener.onPrepareActionMode(mode, menu);
    }
    return updated;
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    for (MultiChoiceModeListener listener : listeners) {
      if (listener.onActionItemClicked(mode, item)) {
        return true;
      }
    }
    return false;
  }

  @Override public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
    for (MultiChoiceModeListener listener : listeners) {
      listener.onItemCheckedStateChanged(mode, position, id, checked);
    }
  }

  @Override public void onDestroyActionMode(ActionMode mode) {
    for (MultiChoiceModeListener listener : listeners) {
      listener.onDestroyActionMode(mode);
    }
  }
}