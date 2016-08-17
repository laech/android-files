package l.files.ui.operations.action;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.view.ActionMode;

import java.util.Collection;

import l.files.fs.Path;
import l.files.ui.operations.R;

import static l.files.operations.OperationService.newDeleteIntent;

public final class DeleteDialog extends AppCompatDialogFragment {

    public static final String FRAGMENT_TAG = "delete-dialog";

    // Null after screen rotation, in that case dismiss dialog
    private final Collection<Path> paths;
    private final ActionMode mode;

    public DeleteDialog() {
        this(null, null);
    }

    @SuppressLint("ValidFragment")
    DeleteDialog(Collection<Path> paths, ActionMode mode) {
        this.paths = paths;
        this.mode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (paths == null || mode == null) {
            setShowsDialog(false);
            dismissAllowingStateLoss();
        }
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getConfirmMessage(paths.size()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestDelete(paths);
                        mode.finish();
                    }
                })
                .create();
    }

    @Override
    public AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    private void requestDelete(Collection<? extends Path> files) {
        Activity context = getActivity();
        context.startService(newDeleteIntent(context, files));
    }

    private String getConfirmMessage(int size) {
        return getActivity().getResources().getQuantityString(
                R.plurals.confirm_delete_question, size, size);
    }

}
