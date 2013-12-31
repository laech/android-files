package l.files.app.mode;

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import l.files.R;
import l.files.app.CloseActionModeRequest;
import l.files.app.FileCreationFragment;
import l.files.provider.FilesContract;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.lang.System.identityHashCode;
import static l.files.app.FilesApp.getBus;
import static l.files.provider.FileCursors.getName;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FilesContract.buildFileUri;
import static org.apache.commons.io.FilenameUtils.getBaseName;

public final class RenameFragment extends FileCreationFragment {

  public static final String TAG = RenameFragment.class.getSimpleName();

  private static final String ARG_FILE_LOCATION = "file_location";

  private static final int LOADER_FILE = identityHashCode(RenameFragment.class);

  static RenameFragment create(String parentLocation, String fileLocation) {
    Bundle args = new Bundle(2);
    args.putString(ARG_PARENT_LOCATION, parentLocation);
    args.putString(ARG_FILE_LOCATION, fileLocation);
    RenameFragment fragment = new RenameFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onStart() {
    super.onStart();
    if (getFilename().isEmpty()) {
      getLoaderManager().restartLoader(LOADER_FILE, null, this);
    }
  }

  @Override protected CharSequence getError(String newFileLocation) {
    if (getFileLocation().equals(newFileLocation)) {
      return null;
    }
    return super.getError(newFileLocation);
  }

  @Override protected int getTitleResourceId() {
    return R.string.rename;
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    if (id == LOADER_FILE) {
      return onCreateFileLoader();
    } else {
      return super.onCreateLoader(id, bundle);
    }
  }

  private Loader<Cursor> onCreateFileLoader() {
    return new CursorLoader(getActivity(), buildFileUri(getFileLocation()),
        null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cr) {
    if (loader.getId() == LOADER_FILE) {
      onFileLoaded(cr);
    } else {
      super.onLoadFinished(loader, cr);
    }
  }

  private void onFileLoaded(Cursor cursor) {
    if (!cursor.moveToFirst() || !getFilename().isEmpty()) {
      return;
    }
    String name = getName(cursor);
    EditText field = getFilenameField();
    field.setText(name);
    if (isDirectory(cursor)) {
      field.selectAll();
    } else {
      field.setSelection(0, getBaseName(name).length());
    }
  }

  @Override public void onClick(DialogInterface dialog, int which) {
    final Context ctx = getActivity();
    final ContentResolver resolver = ctx.getContentResolver();
    new AsyncTask<Void, Void, Boolean>() {

      @Override protected Boolean doInBackground(Void... params) {
        return FilesContract.rename(resolver, getFileLocation(), getFilename());
      }

      @Override protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (!success) {
          makeText(ctx, R.string.failed_to_rename_file, LENGTH_SHORT).show();
        }
      }
    }.execute();
    getBus(this).post(CloseActionModeRequest.INSTANCE);
  }

  private String getFileLocation() {
    return getArguments().getString(ARG_FILE_LOCATION);
  }
}
