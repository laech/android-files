package l.files.ui.browser;

import androidx.appcompat.app.AlertDialog;
import l.files.ui.operations.action.DeleteDialog;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static java.util.Objects.requireNonNull;
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
        awaitOnMainThread(context.getInstrumentation(), () ->
            assertTrue(dialog().getButton(BUTTON_POSITIVE).performClick()));
        return context;
    }

    private AlertDialog dialog() {
        DeleteDialog fragment = (DeleteDialog) context
            .getActivity()
            .getSupportFragmentManager()
            .findFragmentByTag(DeleteDialog.FRAGMENT_TAG);
        assertNotNull(fragment);
        AlertDialog dialog = fragment.getDialog();
        assertNotNull(dialog);
        return dialog;
    }

}
