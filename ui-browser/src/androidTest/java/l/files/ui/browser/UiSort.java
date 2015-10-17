package l.files.ui.browser;

import android.app.Dialog;
import android.widget.ListView;

import l.files.ui.menu.SortDialog;

import static java.util.Objects.requireNonNull;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;

public final class UiSort {

    private final UiFileActivity context;

    public UiSort(UiFileActivity context) {
        requireNonNull(context);
        this.context = context;
    }

    public UiFileActivity by(final FileSort sort) {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                ListView list = listView();
                for (int i = 0; i < list.getChildCount(); i++) {
                    FileSort that = (FileSort) list.getItemAtPosition(i);
                    if (that == sort) {
                        list.performItemClick(list.getChildAt(i), i, i);
                        return;
                    }
                }
                fail();
            }
        });
        return context;
    }

    private ListView listView() {
        return (ListView) dialog().findViewById(android.R.id.list);
    }

    private Dialog dialog() {
        SortDialog fragment = (SortDialog) context
                .activity()
                .getFragmentManager()
                .findFragmentByTag(SortDialog.FRAGMENT_TAG);
        assertNotNull(fragment);
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);
        return dialog;
    }
}
