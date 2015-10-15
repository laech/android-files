package l.files.ui.operations.actions;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ActionMode;

import java.util.Collection;

import l.files.fs.File;
import l.files.operations.OperationService;
import l.files.ui.operations.R;

public final class DeleteDialog extends DialogFragment {

    public static final String FRAGMENT_TAG = "delete-dialog";

    // Null after screen rotation, in that case dismiss dialog
    private final Collection<File> files;
    private final ActionMode mode;

    public DeleteDialog() {
        this(null, null);
    }

    @SuppressLint("ValidFragment")
    DeleteDialog(Collection<File> files, ActionMode mode) {
        this.files = files;
        this.mode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (files == null || mode == null) {
            setShowsDialog(false);
            dismissAllowingStateLoss();
        }
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getConfirmMessage(files.size()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestDelete(files);
                        mode.finish();
                    }
                })
                .create();
    }

    @Override
    public AlertDialog getDialog() {
        return (AlertDialog) super.getDialog();
    }

    private void requestDelete(Collection<? extends File> files) {
        OperationService.delete(getActivity(), files);
    }

    private String getConfirmMessage(int size) {
        return getActivity().getResources().getQuantityString(
                R.plurals.confirm_delete_question, size, size);
    }

}