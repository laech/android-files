package l.files.common.app;

public final class OptionsMenus {

  private static final OptionsMenu EMPTY = new CompositeOptionsMenu();

  private OptionsMenus() {}

  public static OptionsMenu nullToEmpty(OptionsMenu menu) {
    return menu == null ? EMPTY : menu;
  }

  public static OptionsMenu compose(OptionsMenu... actions) {
    return new CompositeOptionsMenu(actions);
  }
}