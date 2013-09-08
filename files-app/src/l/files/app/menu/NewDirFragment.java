package l.files.app.menu;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.content.DialogInterface.OnClickListener;
import static android.text.InputFilter.LengthFilter;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import java.io.File;
import l.files.R;

public final class NewDirFragment extends DialogFragment implements TextWatcher, OnClickListener {

  public static final String TAG = NewDirFragment.class.getSimpleName();

  private static final String ARG_PARENT_DIR = "parent";

  static NewDirFragment create(File parent) {
    Bundle args = new Bundle(1);
    args.putString(ARG_PARENT_DIR, parent.getAbsolutePath());
    NewDirFragment fragment = new NewDirFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private File parent;
  private EditText edit;

  @Override public void onResume() {
    super.onResume();
    getDialog().getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    parent = new File(getArguments().getString(ARG_PARENT_DIR));
    edit = createEditText(getInitialDestinationDir());
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.new_dir)
        .setView(edit)
        .setPositiveButton(android.R.string.ok, this)
        .setNegativeButton(android.R.string.cancel, null)
        .create();
  }

  private EditText createEditText(File dir) {
    final EditText text = new EditText(getActivity());
    text.setId(android.R.id.text1);
    text.setText(dir.getName());
    text.setFilters(new InputFilter[]{new LengthFilter(255)});
    text.setSingleLine();
    text.selectAll();
    text.addTextChangedListener(this);
    return text;
  }

  private File getInitialDestinationDir() {
    File newDir = new File(parent, getString(R.string.untitled_dir));
    for (int i = 2; newDir.exists(); i++) {
      newDir = new File(parent, getString(R.string.untitled_dir_n, i));
    }
    return newDir;
  }

  @Override public void onClick(DialogInterface dialog, int which) {
    boolean created = new File(parent, edit.getText().toString()).mkdir();
    if (!created) {
      makeText(getActivity(), getString(R.string.mkdir_failed), LENGTH_SHORT).show();
    }
  }

  @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
    Button okButton = ((AlertDialog) getDialog()).getButton(BUTTON_POSITIVE);
    if (new File(parent, edit.getText().toString()).exists()) {
      edit.setError(getString(R.string.dir_exists));
      okButton.setEnabled(false);
    } else {
      edit.setError(null);
      okButton.setEnabled(true);
    }
  }

  @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override public void afterTextChanged(Editable s) {}
}
