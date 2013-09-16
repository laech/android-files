package l.files.app;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.content.DialogInterface.OnClickListener;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static org.apache.commons.io.FilenameUtils.getBaseName;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import java.io.File;
import l.files.R;

public abstract class FileCreationFragment extends DialogFragment
    implements OnClickListener {

  private File parent;
  private EditText edit;

  @Override public void onResume() {
    super.onResume();
    getDialog().getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    if (getCurrentDestinationFile().exists()) {
      getDialog().getButton(BUTTON_POSITIVE).setEnabled(false);
    }
  }

  @Override public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    File file = getInitialDestinationFile();
    parent = file.getParentFile();
    edit = createEditTextFor(file);
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.new_dir)
        .setView(edit)
        .setPositiveButton(android.R.string.ok, this)
        .setNegativeButton(android.R.string.cancel, null)
        .create();
  }

  private EditText createEditTextFor(File file) {
    final EditText text = new EditText(getActivity());
    text.setId(android.R.id.text1);
    text.setText(file.getName());
    text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
    text.setSingleLine();
    if (file.isFile()) {
      text.setSelection(0, getBaseName(file.getName()).length());
    } else {
      text.selectAll();
    }
    text.addTextChangedListener(new FileTextWatcher());
    return text;
  }

  @Override public AlertDialog getDialog() {
    return (AlertDialog) super.getDialog();
  }

  protected abstract File getInitialDestinationFile();

  protected File getCurrentDestinationFile() {
    return new File(parent, edit.getText().toString());
  }

  class FileTextWatcher implements TextWatcher {

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      Button ok = getDialog().getButton(BUTTON_POSITIVE);
      if (getCurrentDestinationFile().exists()) {
        edit.setError(edit.getResources().getString(R.string.name_exists));
        ok.setEnabled(false);
      } else {
        edit.setError(null);
        ok.setEnabled(true);
      }
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override public void afterTextChanged(Editable s) {}
  }
}
