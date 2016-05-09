package l.files.ui.browser;

import java.io.IOException;

import l.files.ui.info.InfoBaseFragment;
import l.files.ui.info.InfoFragment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;

public final class UiInfo {

    private final UiFileActivity context;

    public UiInfo(UiFileActivity context) {
        this.context = requireNonNull(context);
    }

    public UiInfo assertName(final String value) throws IOException {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(value, getName());
            }
        });
        return this;
    }

    public UiInfo assertDate(final String value) throws IOException {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(value, getDate());
            }
        });
        return this;
    }

    public UiInfo assertSize(final String value) throws IOException {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(value, getSize());
            }
        });
        return this;
    }


    public UiInfo assertSizeOnDisk(final String value) throws IOException {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertEquals(value, getSizeOnDisk());
            }
        });
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
