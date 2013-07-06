package l.files.ui.menu;

import android.view.Menu;
import com.google.common.collect.ImmutableList;

import java.util.List;

final class CompositeOptionsMenu implements OptionsMenu {

  private final List<OptionsMenu> actions;

  CompositeOptionsMenu(OptionsMenu... actions) {
    this.actions = ImmutableList.copyOf(actions);
  }

  @Override public void onCreate(Menu menu) {
    for (OptionsMenu action : actions) action.onCreate(menu);
  }

  @Override public void onPrepare(Menu menu) {
    for (OptionsMenu action : actions) action.onPrepare(menu);
  }

}
