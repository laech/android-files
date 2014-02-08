package l.files.app.menu;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import l.files.R;
import l.files.app.FileCreationFragment;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.identityHashCode;
import static l.files.app.Fragments.setArgs;
import static l.files.provider.FilesContract.FileInfo.NAME;
import static l.files.provider.FilesContract.buildSuggestionUri;
import static l.files.provider.FilesContract.createDirectory;

public final class NewDirFragment extends FileCreationFragment {

  public static final String TAG = NewDirFragment.class.getSimpleName();


  private static final int LOADER_SUGGESTION =
      identityHashCode(NewDirFragment.class);

  static NewDirFragment create(String parentLocation) {
    return setArgs(new NewDirFragment(), ARG_PARENT_LOCATION, parentLocation);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(LOADER_SUGGESTION, null, this);
  }

  @Override public void onClick(DialogInterface dialog, int which) {
    final Activity context = getActivity();
    final String parentLocation = getParentLocation();
    final String filename = getFilename();
    new AsyncTask<Void, Void, Boolean>() {
      @Override protected Boolean doInBackground(Void... params) {
        return createDirectory(context, parentLocation, filename);
      }

      @Override protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (!success) {
          makeText(context, R.string.mkdir_failed, LENGTH_SHORT).show();
        }
      }
    }.execute();
  }

  @Override protected int getTitleResourceId() {
    return R.string.new_dir;
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    if (id == LOADER_SUGGESTION) {
      return newSuggestionLoader();
    } else {
      return super.onCreateLoader(id, bundle);
    }
  }

  private Loader<Cursor> newSuggestionLoader() {
    String basename = getString(R.string.untitled_dir);
    Activity context = getActivity();
    Uri suggestionUri = buildSuggestionUri(context, getParentLocation(), basename);
    return new CursorLoader(context, suggestionUri, new String[]{NAME},
        null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (loader.getId() == LOADER_SUGGESTION) {
      onSuggestionLoaded(cursor);
    } else {
      super.onLoadFinished(loader, cursor);
    }
  }

  private void onSuggestionLoaded(Cursor cursor) {
    EditText field = getFilenameField();
    if (cursor.moveToFirst() && isNullOrEmpty(field.getText().toString())) {
      field.setText(cursor.getString(0));
      field.selectAll();
    }
  }
}
