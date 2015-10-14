package l.files.ui.base.app;

public final class OptionsMenus {

    public static final OptionsMenu EMPTY = new OptionsMenuAdapter();

    private OptionsMenus() {
    }

    public static OptionsMenu nullToEmpty(OptionsMenu menu) {
        return menu == null ? EMPTY : menu;
    }

    public static OptionsMenu compose(OptionsMenu... menus) {
        return new CompositeMenu(menus);
    }
}
