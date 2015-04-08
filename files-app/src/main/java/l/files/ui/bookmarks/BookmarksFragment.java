package l.files.ui.bookmarks;

import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import l.files.R;
import l.files.common.widget.ListViews;
import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.provider.bookmarks.BookmarkManagerImpl;
import l.files.ui.Animations;
import l.files.ui.BaseFileListFragment;
import l.files.ui.OpenFileRequest;
import l.files.ui.mode.CountSelectedItemsAction;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static l.files.common.widget.MultiChoiceModeListeners.compose;

public final class BookmarksFragment extends BaseFileListFragment
        implements LoaderCallbacks<List<Path>> {

    public BookmarksFragment() {
        super(R.layout.bookmarks_fragment);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addBookmarksHeader();
        setActionMode();
        setListAdapter(new BookmarksAdapter());
        getLoaderManager().initLoader(0, null, this);
    }

    private void setActionMode() {
        ListView list = getListView();
        list.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(compose(
                new CountSelectedItemsAction(this),
                new DeleteAction(BookmarkManagerImpl.get(getActivity()), this)
        ));
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        super.onListItemClick(l, v, pos, id);
        Path path = (Path) l.getItemAtPosition(pos);
        getBus().post(OpenFileRequest.create(path));
    }

    private void addBookmarksHeader() {
        ListView list = getListView();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View header = inflater.inflate(R.layout.bookmark_item_header, list, false);
        ((TextView) header.findViewById(android.R.id.title)).setText(R.string.bookmarks);
        list.addHeaderView(header, null, false);
    }

    @Override
    public BookmarksAdapter getListAdapter() {
        return (BookmarksAdapter) super.getListAdapter();
    }

    @Override
    public Loader<List<Path>> onCreateLoader(int i, Bundle bundle) {
        return new BookmarksLoader(getActivity(), BookmarkManagerImpl.get(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<List<Path>> loader, List<Path> bookmarks) {
        Animations.animatePreDataSetChange(getListView());
        getListAdapter().setItems(bookmarks);
    }

    @Override
    public void onLoaderReset(Loader<List<Path>> loader) {
        getListAdapter().setItems(Collections.<Path>emptyList());
    }

    @Override
    public Resource getCheckedItem() {
        return ((Path) getListView().getItemAtPosition(getCheckedItemPosition())).getResource();
    }

    @Override
    public List<Resource> getCheckedItems() {
        return Lists.transform(ListViews.getCheckedItems(getListView(), Path.class), new Function<Path, Resource>() {
            @Override
            public Resource apply(Path input) {
                return input.getResource();
            }
        });
    }

}
