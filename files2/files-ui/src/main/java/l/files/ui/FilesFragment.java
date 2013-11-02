package l.files.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import static android.os.Environment.getExternalStorageDirectory;
import static android.support.v4.app.LoaderManager.LoaderCallbacks;
import static l.files.provider.FilesContract.buildChildFilesUri;

public final class FilesFragment
    extends ListFragment implements LoaderCallbacks<Cursor> {

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    getLoaderManager().initLoader(0, null, this);
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    return new CursorLoader(
        getActivity(),
        buildChildFilesUri(getExternalStorageDirectory()),
        null,
        null,
        null,
        null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {

  }
}
