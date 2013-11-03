package l.files.common.app;

import static java.util.Arrays.asList;

import java.util.List;

public final class OptionsMenus {

  public static final OptionsMenu EMPTY = new OptionsMenuAdapter();

  private OptionsMenus() {}

  public static OptionsMenu nullToEmpty(OptionsMenu menu) {
    return menu == null ? EMPTY : menu;
  }

  public static OptionsMenu compose(OptionsMenu... menus) {
    return compose(asList(menus));
  }

  public static OptionsMenu compose(List<OptionsMenu> menus) {
    return new CompositeMenu(menus);
  }
}
