package l.files.ui.bookmarks;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.Path;
import l.files.ui.base.fs.OpenFileEvent;
import l.files.ui.base.selection.SelectionModeFragment;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.view.ActionModes;
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode;
import l.files.ui.base.view.CountSelectedItemsAction;

import static java.util.Collections.emptyList;
import static kotlin.collections.CollectionsKt.filterIsInstance;
import static l.files.ui.base.fs.UserDirs.DIR_HOME;

public final class BookmarksFragment
        extends SelectionModeFragment<Path, Path>
        implements LoaderCallbacks<List<Path>> {

    public RecyclerView recycler;
    private BookmarksAdapter adapter;

    public List<Path> bookmarks() {
        return filterIsInstance(adapter.items(), Path.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
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

        View view = getView();
        assert view != null;

        recycler = view.findViewById(R.id.bookmarks);
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
        adapter.setItems(items);
    }

    @Override
    public void onLoaderReset(Loader<List<Path>> loader) {
        adapter.setItems(emptyList());
    }
}
