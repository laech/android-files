package l.files.ui.menu;

import android.view.Menu;
import com.google.common.collect.ImmutableList;

import java.util.List;

public final class OptionsMenu {

  private static final OptionsMenu EMPTY = new OptionsMenu();

  public static OptionsMenu nullToEmpty(OptionsMenu menu) {
    return menu == null ? EMPTY : menu;
  }

  private final List<OptionsMenuAction> actions;

  public OptionsMenu(OptionsMenuAction... actions) {
    this.actions = ImmutableList.copyOf(actions);
  }

  public void onCreateOptionsMenu(Menu menu) {
    for (OptionsMenuAction action : actions) action.onCreate(menu);
  }

  public void onPrepareOptionsMenu(Menu menu) {
    for (OptionsMenuAction action : actions) action.onPrepare(menu);
  }

  public boolean isEmpty() {
    return actions.isEmpty();
  }
}
