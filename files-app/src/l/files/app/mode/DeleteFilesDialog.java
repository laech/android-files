package l.files.app.mode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.google.common.base.Function;
import com.squareup.otto.Bus;
import l.files.R;
import l.files.app.DeleteFilesRequest;

import java.io.File;
import java.util.ArrayList;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static l.files.app.FilesApp.getBus;

public final class DeleteFilesDialog extends DialogFragment {

  public static final String FRAGMENT_TAG = "delete-files-dialog";
  public static final String ARG_PATHS = "paths";

  Bus bus;

  static DeleteFilesDialog create(Iterable<File> files) {
    ArrayList<String> paths = newArrayList();
    for (File file : files) {
      paths.add(file.getAbsolutePath());
    }

    Bundle args = new Bundle(1);
    args.putStringArrayList(ARG_PATHS, paths);

    DeleteFilesDialog dialog = new DeleteFilesDialog();
    dialog.setArguments(args);
    return dialog;
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    bus = getBus(getActivity());
    final ArrayList<String> paths = getArguments().getStringArrayList(ARG_PATHS);
    return new AlertDialog.Builder(getActivity())
        .setMessage(getConfirmMessage(paths.size()))
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            requestDelete(paths);
          }
        })
        .create();
  }

  private void requestDelete(ArrayList<String> paths) {
    bus.post(new DeleteFilesRequest(transform(paths, new Function<String, File>() {
      @Override public File apply(String path) {
        return new File(path);
      }
    })));
  }

  private String getConfirmMessage(int size) {
    return getResources().getQuantityString(
        R.plurals.confirm_delete_question, size, size);
  }
}
