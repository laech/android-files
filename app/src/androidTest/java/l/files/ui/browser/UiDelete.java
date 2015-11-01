package l.files.ui.browser;

import android.app.AlertDialog;

import l.files.ui.operations.actions.DeleteDialog;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static l.files.base.Objects.requireNonNull;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;

final class UiDelete {

    private final UiFileActivity context;

    UiDelete(UiFileActivity context) {
        requireNonNull(context);
        this.context = context;
    }

    UiFileActivity ok() {
        awaitOnMainThread(context.instrumentation(), new Runnable() {
            @Override
            public void run() {
                assertTrue(dialog().getButton(BUTTON_POSITIVE).performClick());
            }
        });
        return context;
    }

    private AlertDialog dialog() {
        DeleteDialog fragment = (DeleteDialog) context
                .activity()
                .getSupportFragmentManager()
                .findFragmentByTag(DeleteDialog.FRAGMENT_TAG);
        assertNotNull(fragment);
        AlertDialog dialog = fragment.getDialog();
        assertNotNull(dialog);
        return dialog;
    }

}
