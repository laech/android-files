package l.files.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import l.files.R;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.content.DialogInterface.OnClickListener;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static java.lang.System.identityHashCode;
import static l.files.provider.FileCursors.getLocation;
import static l.files.provider.FilesContract.buildFileUri;

public abstract class FileCreationFragment extends DialogFragment
    implements OnClickListener, LoaderCallbacks<Cursor> {

  public static final String ARG_PARENT_LOCATION = "parent_location";

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
  }

  @Override public void onStart() {
    super.onStart();
    if (getFilename().isEmpty()) {
      getOkButton().setEnabled(false);
    } else {
      restartChecker();
    }
  }

  @Override public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    editText = createEditText();
    return new AlertDialog.Builder(getActivity())
        .setTitle(getTitleResourceId())
        .setView(editText)
        .setPositiveButton(android.R.string.ok, this)
        .setNegativeButton(android.R.string.cancel, null)
        .create();
  }

  protected CharSequence getError(String newFileLocation) {
    return getString(R.string.name_exists);
  }

  protected abstract int getTitleResourceId();

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
    Activity context = getActivity();
    Uri uri = buildFileUri(context, getParentLocation(), getFilename());
    return new CursorLoader(context, uri, null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (loader.getId() == LOADER_CHECKER) {
      onCheckFinished(cursor);
    }
  }

  private void onCheckFinished(Cursor cursor) {
    Button ok = getOkButton();
    if (cursor.getCount() > 0) {
      cursor.moveToFirst();
      String newFileLocation = getLocation(cursor);
      editText.setError(getError(newFileLocation));
      ok.setEnabled(false);
    } else {
      editText.setError(null);
      ok.setEnabled(true);
    }
  }

  protected String getParentLocation() {
    return getArguments().getString(ARG_PARENT_LOCATION);
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
