package l.files.app;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import l.files.R;
import l.files.analytics.Analytics;

import static android.app.LoaderManager.LoaderCallbacks;
import static l.files.app.Animations.animatePreDataSetChange;
import static l.files.app.FilesApp.getFilesProviderAuthority;
import static l.files.provider.FilesContract.buildBookmarksUri;

public final class SidebarFragment extends BaseFileListFragment
    implements LoaderCallbacks<Cursor> {

  public SidebarFragment() {
    super(R.layout.sidebar_fragment);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    addBookmarksHeader();
    setListAdapter(new SidebarAdapter());
    getLoaderManager().initLoader(0, null, this);
  }

  private void addBookmarksHeader() {
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    View header = inflater.inflate(R.layout.sidebar_item_header, null, false);
    ((TextView) header.findViewById(android.R.id.title))
        .setText(R.string.bookmarks);
    getListView().addHeaderView(header, null, false);
  }

  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Analytics.onEvent(getActivity(), "sidebar", "click");
  }

  @Override public SidebarAdapter getListAdapter() {
    return (SidebarAdapter) super.getListAdapter();
  }

  @Override public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    Uri authority = getFilesProviderAuthority(this);
    return new CursorLoader(getActivity(), buildBookmarksUri(authority),
        null, null, null, null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    animatePreDataSetChange(getListView());
    getListAdapter().setCursor(cursor);
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {
    getListAdapter().setCursor(null);
  }
}
