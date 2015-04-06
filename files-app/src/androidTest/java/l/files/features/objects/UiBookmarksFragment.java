package l.files.features.objects;

import android.app.Instrumentation;
import android.view.View;
import android.widget.ListView;

import com.google.common.base.Predicate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import l.files.R;
import l.files.fs.Path;
import l.files.ui.FilesActivity;
import l.files.ui.bookmarks.BookmarksFragment;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;

public final class UiBookmarksFragment {

    private final Instrumentation instrument;
    private final BookmarksFragment fragment;

    public UiBookmarksFragment(Instrumentation instrument, BookmarksFragment fragment) {
        this.instrument = instrument;
        this.fragment = fragment;
    }

    private ListView getListView() {
        return (ListView) fragment.getView();
    }

    private FilesActivity getActivity() {
        return (FilesActivity) fragment.getActivity();
    }

    public UiFileActivity getActivityObject() {
        return new UiFileActivity(instrument, getActivity());
    }

    public UiFileActivity selectBookmark(final Path path) throws IOException {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                click(getListView(), new Predicate<Object>() {
                    @Override
                    public boolean apply(Object input) {
                        return input.equals(path);
                    }
                });
            }
        });

        if (path.getResource().readStatus(false).isDirectory()) {
            getActivityObject().assertCurrentDirectory(path);
        }
        return getActivityObject();
    }

    public UiBookmarksFragment checkBookmark(final Path bookmark, final boolean checked) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                int i = indexOf(getListView(), new Predicate<Object>() {
                    @Override
                    public boolean apply(Object input) {
                        return input.equals(bookmark);
                    }
                });
                getListView().setItemChecked(i, checked);
            }
        });
        return this;

    }

    public UiBookmarksFragment deleteCheckedBookmarks() {
        getActivityObject().selectActionModeAction(R.id.delete_selected_bookmarks);
        getActivityObject().waitForActionModeToFinish();
        return this;
    }

    private List<Path> getBookmarks() {
        List<Path> paths = new ArrayList<>();
        for (int i = getListView().getHeaderViewsCount(); i < getListView().getCount(); i++) {
            paths.add((Path) getListView().getItemAtPosition(i));
        }
        return paths;
    }

    public UiBookmarksFragment assertCurrentDirectoryBookmarked(final boolean bookmarked) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                Path path = getActivity().getCurrentPagerFragment().getCurrentPath();
                List<Path> paths = getBookmarks();
                assertEquals(paths.toString(), bookmarked, paths.contains(path));
            }
        });
        return this;
    }

    public UiBookmarksFragment assertBookmarked(final Path bookmark, final boolean bookmarked) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(bookmarked, getBookmarks().contains(bookmark));
            }
        });
        return this;
    }

    public UiBookmarksFragment assertContainsBookmarksInOrder(final Path... paths) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                List<Path> expected = asList(paths);
                List<Path> actual = new ArrayList<>();
                for (Path bookmark : getBookmarks()) {
                    if (expected.contains(bookmark)) {
                        actual.add(bookmark);
                    }
                }
                assertEquals(expected, actual);
            }
        });
        return this;
    }

    private int indexOf(ListView list, Predicate<Object> pred) {
        for (int i = list.getHeaderViewsCount(); i < list.getCount(); i++) {
            if (pred.apply(list.getItemAtPosition(i))) {
                return i;
            }
        }
        throw new AssertionError("Not found");
    }

    private void click(ListView list, Predicate<Object> pred) {
        int position = indexOf(list, pred);
        int firstVisiblePosition = list.getFirstVisiblePosition();
        int viewPosition = position - firstVisiblePosition;
        View view = list.getChildAt(viewPosition);
        assertTrue(list.performItemClick(view, viewPosition, position));
    }

}
