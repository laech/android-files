package l.files.ui.menu;

public final class OptionsMenus {

  private static final OptionsMenu EMPTY = new CompositeOptionsMenu();

  public static OptionsMenu nullToEmpty(OptionsMenu menu) {
    return menu == null ? EMPTY : menu;
  }

  public static OptionsMenu compose(OptionsMenu... actions) {
    return new CompositeOptionsMenu(actions);
  }

  private OptionsMenus() {}

}