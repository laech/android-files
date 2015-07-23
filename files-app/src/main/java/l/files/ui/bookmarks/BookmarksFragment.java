package l.files.ui.bookmarks;

import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import l.files.R;
import l.files.common.view.ActionModeProvider;
import l.files.common.widget.ActionModes;
import l.files.fs.Resource;
import l.files.provider.bookmarks.BookmarkManagerImpl;
import l.files.ui.browser.OnOpenFileListener;
import l.files.ui.mode.ClearSelectionOnDestroyActionMode;
import l.files.ui.mode.CountSelectedItemsAction;
import l.files.ui.selection.SelectionModeFragment;

import static android.app.LoaderManager.LoaderCallbacks;
import static l.files.common.view.Views.find;

public final class BookmarksFragment
    extends SelectionModeFragment<Resource>
    implements LoaderCallbacks<List<Resource>> {

  public RecyclerView recycler;
  private BookmarksAdapter adapter;

  public List<Resource> bookmarks() {
    return adapter.items();
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.bookmarks_fragment, container, false);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter = new BookmarksAdapter(
        selection(),
        actionModeProvider(),
        actionModeCallback(),
        (OnOpenFileListener) getActivity());

    recycler = find(R.id.bookmarks, this);
    recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    recycler.setAdapter(adapter);

    getLoaderManager().initLoader(0, null, this);
  }

  @Override protected ActionMode.Callback actionModeCallback() {
    return ActionModes.compose(
        new CountSelectedItemsAction(selection()),
        new ClearSelectionOnDestroyActionMode(selection()),
        new DeleteAction(BookmarkManagerImpl.get(getActivity()), selection())
    );
  }

  @Override protected ActionModeProvider actionModeProvider() {
    return (ActionModeProvider) getActivity();
  }

  @Override
  public Loader<List<Resource>> onCreateLoader(int id, Bundle bundle) {
    return new BookmarksLoader(
        getActivity(),
        BookmarkManagerImpl.get(getActivity()));
  }

  @Override public void onLoadFinished(
      Loader<List<Resource>> loader, List<Resource> bookmarks) {
    adapter.setItems(bookmarks);
  }

  @Override public void onLoaderReset(Loader<List<Resource>> loader) {
    adapter.setItems(Collections.<Resource>emptyList());
  }
}
