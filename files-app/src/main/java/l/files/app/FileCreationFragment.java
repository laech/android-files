package l.files.app;

import android.app.AlertDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import l.files.R;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.content.DialogInterface.OnClickListener;
import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static android.view.WindowManager.LayoutParams
    .SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static java.lang.System.identityHashCode;
import static l.files.provider.FilesContract.buildFileUri;

public abstract class FileCreationFragment extends DialogFragment
    implements OnClickListener, LoaderCallbacks<Cursor> {

  public static final String ARG_PARENT_ID = "parent_id";

  private static final int LOADER_CHECKER =
      identityHashCode(FileCreationFragment.class);

  private EditText editText;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NORMAL, R.style.Theme_Dialog);
  }

  @Override public void onResume() {
    super.onResume();
    getDialog().getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    if (getFilename().isEmpty()) {
      getOkButton().setEnabled(false);
    } else {
      restartChecker();
    }
  }

  @Override public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    editText = createEditText();
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.new_dir)
        .setView(editText)
        .setPositiveButton(android.R.string.ok, this)
        .setNegativeButton(android.R.string.cancel, null)
        .create();
  }

  private EditText createEditText() {
    final EditText text = new EditText(getActivity());
    text.setId(android.R.id.text1);
    text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
    text.setSingleLine();
    text.addTextChangedListener(new FileTextWatcher());
    return text;
  }

  @Override public AlertDialog getDialog() {
    return (AlertDialog) super.getDialog();
  }

  private void restartChecker() {
    getLoaderManager().restartLoader(LOADER_CHECKER, null, this);
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    if (id == LOADER_CHECKER) {
      return newChecker();
    }
    return null;
  }

  private Loader<Cursor> newChecker() {
    Uri uri = buildFileUri(getParentId(), getFilename());
    return new CursorLoader(getActivity(), uri, null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (loader.getId() == LOADER_CHECKER) {
      onCheckFinished(cursor);
    }
  }

  private void onCheckFinished(Cursor cursor) {
    Button ok = getOkButton();
    if (cursor.getCount() > 0) {
      editText.setError(getString(R.string.name_exists));
      ok.setEnabled(false);
    } else {
      editText.setError(null);
      ok.setEnabled(true);
    }
  }

  protected String getParentId() {
    return getArguments().getString(ARG_PARENT_ID);
  }

  protected String getFilename() {
    return editText.getText().toString();
  }

  protected EditText getFilenameField() {
    return editText;
  }

  private Button getOkButton() {
    return getDialog().getButton(BUTTON_POSITIVE);
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {}

  class FileTextWatcher implements TextWatcher {

    @Override public void onTextChanged(
        CharSequence s, int start, int before, int count) {
      if (getFilename().isEmpty()) {
        getOkButton().setEnabled(false);
      } else {
        restartChecker();
      }
    }


    @Override public void beforeTextChanged(
        CharSequence s, int start, int count, int after) {}

    @Override public void afterTextChanged(Editable s) {}
  }
}
