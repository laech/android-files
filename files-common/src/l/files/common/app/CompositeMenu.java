package l.files.common.app;

import android.view.Menu;
import com.google.common.collect.ImmutableList;
import java.util.List;

final class CompositeMenu implements OptionsMenu {

  private final List<OptionsMenu> actions;

  CompositeMenu(OptionsMenu... actions) {
    this.actions = ImmutableList.copyOf(actions);
  }

  CompositeMenu(Iterable<OptionsMenu> actions) {
    this.actions = ImmutableList.copyOf(actions);
  }

  @Override public void onCreate(Menu menu) {
    for (OptionsMenu action : actions) {
      action.onCreate(menu);
    }
  }

  @Override public void onPrepare(Menu menu) {
    for (OptionsMenu action : actions) {
      action.onPrepare(menu);
    }
  }
}
