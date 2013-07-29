package l.files.common.widget;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Static utility methods pertaining to {@code Viewer} instances.
 */
public final class MultiChoiceActions {
  private MultiChoiceActions() {}

  /**
   * Returns a composition of the given actions.
   */
  public static MultiChoiceAction compose(MultiChoiceAction... actions) {
    return new Composition(actions);
  }

  /**
   * Returns a {@link MultiChoiceModeListener} backed by the given actions.
   */
  public static MultiChoiceModeListener asListener(final MultiChoiceAction... actions) {
    return new ListenerAdapter(compose(actions));
  }

  private static class Composition implements MultiChoiceAction {
    private final List<MultiChoiceAction> actions;

    Composition(MultiChoiceAction... actions) {
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
  }

  private static class ListenerAdapter implements MultiChoiceModeListener {
    private final MultiChoiceAction mode;

    ListenerAdapter(MultiChoiceAction mode) {
      this.mode = checkNotNull(mode, "mode");
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
      mode.onChange(actionMode, i, l, b);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
      mode.onCreate(actionMode, menu);
      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
      return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
      return true;
    }

    @Override public void onDestroyActionMode(ActionMode mode) {}
  }
}
