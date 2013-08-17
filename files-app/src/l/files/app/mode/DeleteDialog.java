package l.files.app.mode;

import static l.files.app.FilesApp.getBus;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.common.io.Files.toFiles;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.squareup.otto.Bus;
import java.io.File;
import l.files.R;
import l.files.app.DeleteRequest;

public final class DeleteDialog extends DialogFragment {

  public static final String FRAGMENT_TAG = "delete-files-dialog";
  public static final String ARG_PATHS = "paths";

  Bus bus;

  static DeleteDialog create(File... files) {
    Bundle args = new Bundle(1);
    args.putStringArray(ARG_PATHS, toAbsolutePaths(files));

    DeleteDialog dialog = new DeleteDialog();
    dialog.setArguments(args);
    return dialog;
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    bus = getBus(getActivity());
    final String[] paths = getArguments().getStringArray(ARG_PATHS);
    return new AlertDialog.Builder(getActivity())
        .setMessage(getConfirmMessage(paths.length))
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            requestDelete(paths);
          }
        })
        .create();
  }

  private void requestDelete(String[] paths) {
    if (0 != paths.length) bus.post(new DeleteRequest(toFiles(paths)));
  }

  private String getConfirmMessage(int size) {
    return getResources().getQuantityString(
        R.plurals.confirm_delete_question, size, size);
  }
}
