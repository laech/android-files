package l.files.ui.browser;

import java.io.IOException;

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

    UiInfo assertName(String value) throws IOException {
        awaitOnMainThread(context.instrumentation(), () -> assertEquals(value, getName()));
        return this;
    }

    UiInfo assertDate(String value) throws IOException {
        awaitOnMainThread(context.instrumentation(), () -> assertEquals(value, getDate()));
        return this;
    }

    UiInfo assertSize(String value) throws IOException {
        awaitOnMainThread(context.instrumentation(), () -> assertEquals(value, getSize()));
        return this;
    }


    UiInfo assertSizeOnDisk(String value) throws IOException {
        awaitOnMainThread(context.instrumentation(), () -> assertEquals(value, getSizeOnDisk()));
        return this;
    }

    private String getName() {
        return ((InfoFragment) fragment()).getNameView().getText().toString();
    }

    private String getDate() {
        return ((InfoFragment) fragment()).getDateView().getText().toString();
    }

    private String getSize() {
        return fragment().getSizeView().getText().toString();
    }

    private String getSizeOnDisk() {
        return fragment().getSizeOnDiskView().getText().toString();
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
