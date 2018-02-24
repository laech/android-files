package l.files.ui.base.app;

public final class OptionsMenus {

    static final OptionsMenu EMPTY = new OptionsMenuAdapter();

    private OptionsMenus() {
    }

    public static OptionsMenu compose(OptionsMenu... menus) {
        return new CompositeMenu(menus);
    }
}
