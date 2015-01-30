package l.files.ui;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.fs.FileStatus;
import l.files.fs.FileSystemException;
import l.files.fs.Path;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static java.lang.System.identityHashCode;

public abstract class FileCreationFragment extends DialogFragment
    implements DialogInterface.OnClickListener {

  public static final String ARG_PARENT_PATH = "parent";

  private static final int LOADER_CHECKER =
      identityHashCode(FileCreationFragment.class);

  private LoaderCallbacks<FileStatus> checkerCallback = new CheckerCallback();
  private EditText editText;

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

  protected CharSequence getError(Path target) {
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
    getLoaderManager().restartLoader(LOADER_CHECKER, null, checkerCallback);
  }

  protected Path getParentPath() {
    return getArguments().getParcelable(ARG_PARENT_PATH);
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


  class CheckerCallback implements LoaderCallbacks<FileStatus> {

    @Override public Loader<FileStatus> onCreateLoader(int id, Bundle bundle) {
      if (id == LOADER_CHECKER) {
        return newChecker();
      }
      return null;
    }

    private Loader<FileStatus> newChecker() {
      final Path path = getParentPath().resolve(getFilename());
      return new AsyncTaskLoader<FileStatus>(getActivity()) {
        @Override public FileStatus loadInBackground() {
          try {
            return path.getResource().stat();
          } catch (IOException | FileSystemException e) { // TODO
            return null;
          }
        }

        @Override protected void onStartLoading() {
          super.onStartLoading();
          forceLoad();
        }
      };
    }

    @Override public void onLoadFinished(Loader<FileStatus> loader, FileStatus stat) {
      if (loader.getId() == LOADER_CHECKER) {
        onCheckFinished(stat);
      }
    }

    @Override public void onLoaderReset(Loader<FileStatus> loader) {}

    private void onCheckFinished(FileStatus stat) {
      Button ok = getOkButton();
      if (stat != null) {
        editText.setError(getError(stat.path()));
        ok.setEnabled(false);
      } else {
        editText.setError(null);
        ok.setEnabled(true);
      }
    }
  }

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
