package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import com.google.common.collect.ImmutableList;

import java.util.List;

final class CompositeMultiChoiceMode implements MultiChoiceMode {

  private final List<MultiChoiceMode> actions;

  CompositeMultiChoiceMode(MultiChoiceMode... actions) {
    this.actions = ImmutableList.copyOf(actions);
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    for (MultiChoiceMode action : actions) action.onCreate(mode, menu);
  }

  @Override
  public void onChange(ActionMode mode, int position, long id, boolean checked) {
    for (MultiChoiceMode action : actions)
      action.onChange(mode, position, id, checked);
  }
}
