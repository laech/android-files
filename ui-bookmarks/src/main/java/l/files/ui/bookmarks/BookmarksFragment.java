package l.files.ui.bookmarks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import l.files.bookmarks.BookmarksManager;
import l.files.bookmarks.BookmarksKt;
import l.files.fs.Path;
import l.files.ui.base.fs.OpenFileEvent;
import l.files.ui.base.selection.SelectionModeFragment;
import l.files.ui.base.view.ActionModeProvider;
import l.files.ui.base.view.ActionModes;
import l.files.ui.base.view.ClearSelectionOnDestroyActionMode;
import l.files.ui.base.view.CountSelectedItemsAction;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import static kotlin.collections.CollectionsKt.filterIsInstance;
import static l.files.bookmarks.BookmarksKt.getBookmarkManager;
import static l.files.ui.base.fs.UserDirs.DIR_HOME;

public final class BookmarksFragment
        extends SelectionModeFragment<Path, Path>
        implements Observer<List<Path>> {

    public RecyclerView recycler;
    private BookmarksAdapter adapter;
    private BookmarksManager bookmarks;

    public List<Path> bookmarks() {
        return filterIsInstance(adapter.items(), Path.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.bookmarks_fragment, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        bookmarks = getBookmarkManager(this);

        Transformations.map(
                bookmarks.getLiveData(),
                paths -> BookmarksKt.collate(
                        paths,
                        path -> path.equals(DIR_HOME),
                        Collator.getInstance()
                )
        ).observe(getViewLifecycleOwner(), this);
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
    }

    @Override
    protected ActionMode.Callback actionModeCallback() {
        return ActionModes.compose(
                new CountSelectedItemsAction(selection()),
                new ClearSelectionOnDestroyActionMode(selection()),
                new RemoveBookmark(selection(), bookmarks)
        );
    }

    @Override
    protected ActionModeProvider actionModeProvider() {
        return (ActionModeProvider) getActivity();
    }

    @Override
    public void onChanged(List<Path> bookmarks) {
        List<Object> items = new ArrayList<>(bookmarks.size() + 1);
        items.add(getString(R.string.bookmarks));
        items.addAll(bookmarks);
        adapter.setItems(items);
    }
}
