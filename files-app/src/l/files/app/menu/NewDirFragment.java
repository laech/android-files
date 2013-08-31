package l.files.app.menu;

import static android.content.DialogInterface.OnClickListener;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import java.io.File;
import l.files.R;

public final class NewDirFragment extends DialogFragment {

  private static final String ARG_PARENT_DIR = "parent";

  static NewDirFragment create(File parent) {
    Bundle args = new Bundle(1);
    args.putString(ARG_PARENT_DIR, parent.getAbsolutePath());
    NewDirFragment fragment = new NewDirFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    final File newDir = getDestinationDir();
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.new_dir)
        .setView(createEditText(newDir))
        .setPositiveButton(android.R.string.ok, createDirListener(newDir))
        .setNegativeButton(android.R.string.cancel, null)
        .create();
  }

  private OnClickListener createDirListener(final File dir) {
    return new OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        boolean created = dir.mkdir();
        if (!created) {
          makeText(getActivity(), getString(R.string.mkdir_failed), LENGTH_SHORT).show();
        }
      }
    };
  }

  private EditText createEditText(File dir) {
    EditText text = new EditText(getActivity());
    text.setText(dir.getName());
    text.selectAll();
    return text;
  }

  private File getDestinationDir() {
    File parent = new File(getArguments().getString(ARG_PARENT_DIR));
    File newDir = new File(parent, getString(R.string.untitled_dir));
    for (int i = 2; newDir.exists(); i++) {
      newDir = new File(parent, getString(R.string.untitled_dir_n, i));
    }
    return newDir;
  }
}
