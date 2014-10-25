package l.files.ui.mode;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import java.io.IOException;

import l.files.R;
import l.files.ui.CloseActionModeRequest;
import l.files.ui.FileCreationFragment;
import l.files.provider.FilesContract;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.lang.System.identityHashCode;
import static l.files.ui.FilesApp.getBus;
import static l.files.provider.FilesContract.Files;
import static l.files.provider.FilesContract.Files.isDirectory;
import static l.files.provider.FilesContract.getFileUri;
import static org.apache.commons.io.FilenameUtils.getBaseName;

public final class RenameFragment extends FileCreationFragment {

  public static final String TAG = RenameFragment.class.getSimpleName();

  private static final String ARG_FILE_LOCATION = "file_location";

  private static final int LOADER_FILE = identityHashCode(RenameFragment.class);

  private LoaderCallbacks<Cursor> fileCallback = new FileCallback();

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
      getLoaderManager().restartLoader(LOADER_FILE, null, fileCallback);
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

  @Override public void onClick(DialogInterface dialog, int which) {
    final Context context = getActivity();
    new AsyncTask<Void, Void, IOException>() {

      @Override protected IOException doInBackground(Void... params) {
        try {
          FilesContract.rename(context, getFileLocation(), getFilename());
          return null;
        } catch (IOException e) {
          return e;
        }
      }

      @Override protected void onPostExecute(IOException e) {
        super.onPostExecute(e);
        if (e != null) {
          String message = context.getString(R.string.failed_to_rename_file_x, e.getMessage());
          makeText(context, message, LENGTH_SHORT).show();
        }
      }
    }.execute();
    getBus(this).post(CloseActionModeRequest.INSTANCE);
  }

  private String getFileLocation() {
    return getArguments().getString(ARG_FILE_LOCATION);
  }

  class FileCallback implements LoaderCallbacks<Cursor> {

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
      return onCreateFileLoader();
    }

    private Loader<Cursor> onCreateFileLoader() {
      Activity context = getActivity();
      return new CursorLoader(context, getFileUri(context, getFileLocation()),
          null, null, null, null);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cr) {
      onFileLoaded(cr);
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {}

    private void onFileLoaded(Cursor cursor) {
      if (!cursor.moveToFirst() || !getFilename().isEmpty()) {
        return;
      }
      String name = Files.name(cursor);
      EditText field = getFilenameField();
      field.setText(name);
      if (isDirectory(cursor)) {
        field.selectAll();
      } else {
        field.setSelection(0, getBaseName(name).length());
      }
    }
  }
}
