package l.files.common.app;

import android.view.Menu;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Static utility methods pertaining to {@code OptionsMenu} instances.
 */
public final class OptionsMenus {

  private static final OptionsMenu EMPTY = new OptionsMenuAdapter();

  private OptionsMenus() {}

  /**
   * If the menu is not null, returns the menu; if the menu is null, returns an
   * empty menu that does nothing.
   */
  public static OptionsMenu nullToEmpty(OptionsMenu menu) {
    return menu == null ? EMPTY : menu;
  }

  /**
   * Returns a composition of the given menus.
   */
  public static OptionsMenu compose(OptionsMenu... menus) {
    return new Composition(menus);
  }

  private static class Composition implements OptionsMenu {
    private final List<OptionsMenu> actions;

    Composition(OptionsMenu... actions) {
      this.actions = ImmutableList.copyOf(actions);
    }

    @Override public void onCreate(Menu menu) {
      for (OptionsMenu action : actions) action.onCreate(menu);
    }

    @Override public void onPrepare(Menu menu) {
      for (OptionsMenu action : actions) action.onPrepare(menu);
    }

    @Override public void onClose(Menu menu) {
      for (OptionsMenu action : actions) action.onClose(menu);
    }
  }
}