package l.files.app;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.net.URI;

import l.files.R;
import l.files.common.app.BaseListFragment;

import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.buildBookmarksUri;

public final class SidebarFragment extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.sidebar_fragment, container, false);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    addBookmarksHeader(savedInstanceState);
    setListAdapter(new SidebarAdapter());
    getLoaderManager().initLoader(0, null, this);
  }

  private void addBookmarksHeader(Bundle savedInstanceState) {
    LayoutInflater inflater = getLayoutInflater(savedInstanceState);
    View header = inflater.inflate(R.layout.sidebar_item_header, null, false);
    ((TextView) header.findViewById(android.R.id.title))
        .setText(R.string.bookmarks);
    getListView().addHeaderView(header, null, false);
  }

  @Override public SidebarAdapter getListAdapter() {
    return (SidebarAdapter) super.getListAdapter();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Cursor cursor = (Cursor) l.getItemAtPosition(position);
    String uri = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
    FilesApp.getBus(this).post(new OpenFileRequest(new File(URI.create(uri))));
    // TODO
  }

  @Override public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return new CursorLoader(getActivity(), buildBookmarksUri(),
        null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    getListAdapter().setCursor(cursor);
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {
    getListAdapter().setCursor(null);
  }
}
