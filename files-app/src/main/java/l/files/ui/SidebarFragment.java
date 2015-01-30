package l.files.ui;

import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import l.files.R;
import l.files.fs.Path;
import l.files.provider.bookmarks.BookmarkManagerImpl;
import l.files.provider.bookmarks.BookmarksLoader;

import static android.app.LoaderManager.LoaderCallbacks;

public final class SidebarFragment extends BaseFileListFragment
    implements LoaderCallbacks<List<Path>> {

  public SidebarFragment() {
    super(R.layout.sidebar_fragment);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    addBookmarksHeader();
    setListAdapter(new SidebarAdapter());
    getLoaderManager().initLoader(0, null, this);
  }


  @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    super.onListItemClick(l, v, pos, id);
    Path path = (Path) l.getItemAtPosition(pos);
    try {
      getBus().post(OpenFileRequest.create(path.getResource().stat()));
    } catch (IOException e) {
      throw new RuntimeException(); // TODO
    }
  }

  private void addBookmarksHeader() {
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    View header = inflater.inflate(R.layout.sidebar_item_header, null, false);
    ((TextView) header.findViewById(android.R.id.title))
        .setText(R.string.bookmarks);
    getListView().addHeaderView(header, null, false);
  }

  @Override public SidebarAdapter getListAdapter() {
    return (SidebarAdapter) super.getListAdapter();
  }

  @Override public Loader<List<Path>> onCreateLoader(int i, Bundle bundle) {
    return new BookmarksLoader(getActivity(), BookmarkManagerImpl.get(getActivity()));
  }

  @Override public void onLoadFinished(Loader<List<Path>> loader, List<Path> bookmarks) {
    Animations.animatePreDataSetChange(getListView());
    getListAdapter().setItems(bookmarks);
  }

  @Override public void onLoaderReset(Loader<List<Path>> loader) {
    getListAdapter().setItems(Collections.<Path>emptyList());
  }
}
