package l.files.ui.browser;

import l.files.ui.info.InfoBaseFragment;
import l.files.ui.info.InfoFragment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;

final class UiInfo {

    private final UiFileActivity context;

    UiInfo(UiFileActivity context) {
        this.context = requireNonNull(context);
    }

    UiInfo assertName(String value) {
        awaitOnMainThread(context.instrumentation(), () -> assertEquals(value, getName()));
        return this;
    }

    UiInfo assertDate(String value) {
        awaitOnMainThread(context.instrumentation(), () -> assertEquals(value, getDate()));
        return this;
    }

    UiInfo assertSize(String value) {
        awaitOnMainThread(context.instrumentation(), () -> assertEquals(value, getSize()));
        return this;
    }

    private String getName() {
        return ((InfoFragment) fragment()).getDisplayedName().toString();
    }

    private String getDate() {
        return ((InfoFragment) fragment()).getDisplayedLastModifiedTime().toString();
    }

    private String getSize() {
        return fragment().getDisplayedSize().toString();
    }

    private InfoBaseFragment fragment() {
        InfoBaseFragment fragment = (InfoBaseFragment) context
                .activity()
                .getSupportFragmentManager()
                .findFragmentByTag(InfoBaseFragment.FRAGMENT_TAG);
        assertNotNull(fragment);
        return fragment;
    }

}
