package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import com.google.common.collect.ImmutableList;

import java.util.List;

final class CompositeMultiChoiceAction implements MultiChoiceAction {

  private final List<MultiChoiceAction> actions;

  CompositeMultiChoiceAction(MultiChoiceAction... actions) {
    this.actions = ImmutableList.copyOf(actions);
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    for (MultiChoiceAction action : actions) {
      action.onCreate(mode, menu);
    }
  }

  @Override
  public void onChange(ActionMode mode, int position, long id, boolean checked) {
    for (MultiChoiceAction action : actions) {
      action.onChange(mode, position, id, checked);
    }
  }

  @Override public void onDestroy(ActionMode mode) {
    for (MultiChoiceAction action : actions) {
      action.onDestroy(mode);
    }
  }
}
