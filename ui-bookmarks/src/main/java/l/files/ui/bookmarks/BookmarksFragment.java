package l.files.ui.bookmarks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.Path;
import l.files.ui.base.fs.OpenFileEvent;
import l.files.ui.base.selection.SelectionModeFragment;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.view.ActionModes;
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode;
import l.files.ui.base.view.CountSelectedItemsAction;

import static l.files.ui.base.fs.UserDirs.DIR_HOME;

public final class BookmarksFragment
        extends SelectionModeFragment<Path, Path>
        implements LoaderCallbacks<List<Path>> {

    @Nullable
    public RecyclerView recycler;

    @Nullable
    private BookmarksAdapter adapter;

    public List<Path> bookmarks() {
        assert adapter != null;
        List<Object> items = adapter.items();
        List<Path> bookmarks = new ArrayList<>(items.size());
        for (Object item : items) {
            if (item instanceof Path) {
                bookmarks.add((Path) item);
            }
        }
        return bookmarks;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmarks_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new BookmarksAdapter(
                selection(),
                actionModeProvider(),
                actionModeCallback(),
                OpenFileEvent.topic
        );

        recycler = getView().findViewById(R.id.bookmarks);
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
    public Loader<List<Path>> onCreateLoader(int id, Bundle bundle) {
        return new BookmarksLoader(
                getActivity(),
                BookmarkManager.get(getActivity()),
                DIR_HOME);
    }

    @Override
    public void onLoadFinished(Loader<List<Path>> loader, List<Path> bookmarks) {
        List<Object> items = new ArrayList<>(bookmarks.size() + 1);
        items.add(getString(R.string.bookmarks));
        items.addAll(bookmarks);
        assert adapter != null;
        adapter.setItems(items);
    }

    @Override
    public void onLoaderReset(Loader<List<Path>> loader) {
        assert adapter != null;
        adapter.setItems(Collections.<Path>emptyList());
    }
}
