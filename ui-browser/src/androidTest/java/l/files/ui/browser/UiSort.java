package l.files.ui.browser;

import android.app.Dialog;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import l.files.ui.browser.menu.SortDialog;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static l.files.base.Objects.requireNonNull;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;

final class UiSort {

    private final UiFileActivity context;

    UiSort(UiFileActivity context) {
        requireNonNull(context);
        this.context = context;
    }

    UiFileActivity by(FileSort sort) {
        awaitOnMainThread(context.instrumentation(), () -> {
            ListView list = listView();
            for (int i = 0; i < list.getChildCount(); i++) {
                FileSort that = (FileSort) list.getItemAtPosition(i);
                if (that == sort) {
                    list.performItemClick(list.getChildAt(i), i, i);
                    return;
                }
            }
            fail();
        });
        return context;
    }

    private ListView listView() {
        return dialog().getListView();
    }

    private AlertDialog dialog() {
        SortDialog fragment = (SortDialog) context
                .activity()
                .getSupportFragmentManager()
                .findFragmentByTag(SortDialog.FRAGMENT_TAG);
        assertNotNull(fragment);
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);
        return (AlertDialog) dialog;
    }
}
