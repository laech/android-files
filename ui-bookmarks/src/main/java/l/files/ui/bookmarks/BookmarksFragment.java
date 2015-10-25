package l.files.ui.bookmarks;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.File;
import l.files.ui.base.fs.OnOpenFileListener;
import l.files.ui.base.selection.SelectionModeFragment;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.view.ActionModes;
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode;
import l.files.ui.base.view.CountSelectedItemsAction;

import static l.files.ui.base.view.Views.find;

public final class BookmarksFragment
        extends SelectionModeFragment<File>
        implements LoaderCallbacks<List<File>> {

    public RecyclerView recycler;
    private BookmarksAdapter adapter;

    public List<File> bookmarks() {
        List<Object> items = adapter.items();
        List<File> bookmarks = new ArrayList<>(items.size());
        for (Object item : items) {
            if (item instanceof File) {
                bookmarks.add((File) item);
            }
        }
        return bookmarks;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmarks_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
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

    @Override
    protected ActionMode.Callback actionModeCallback() {
        return ActionModes.compose(
                new CountSelectedItemsAction(selection()),
                new ClearSelectionOnDestroyActionMode(selection()),
                new RemoveBookmark(selection(), BookmarkManager.get(getActivity()))
        );
    }

    @Override
    protected ActionModeProvider actionModeProvider() {
        return (ActionModeProvider) getActivity();
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle bundle) {
        return new BookmarksLoader(
                getActivity(),
                BookmarkManager.get(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> bookmarks) {
        List<Object> items = new ArrayList<>(bookmarks.size() + 1);
        items.add(getString(R.string.bookmarks));
        items.addAll(bookmarks);
        adapter.setItems(items);
    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {
        adapter.setItems(Collections.<File>emptyList());
    }
}
