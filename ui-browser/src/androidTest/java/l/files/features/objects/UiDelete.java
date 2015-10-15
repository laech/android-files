package l.files.features.objects;

import android.app.AlertDialog;

import l.files.ui.operations.actions.DeleteDialog;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static java.util.Objects.requireNonNull;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;

public final class UiDelete {

    private final UiFileActivity context;

    public UiDelete(UiFileActivity context) {
        requireNonNull(context);
        this.context = context;
    }

    public UiFileActivity ok() {
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
                .getFragmentManager()
                .findFragmentByTag(DeleteDialog.FRAGMENT_TAG);
        assertNotNull(fragment);
        AlertDialog dialog = fragment.getDialog();
        assertNotNull(dialog);
        return dialog;
    }

}
