package l.files.ui.bookmarks;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.provider.bookmarks.BookmarkManager;

import static java.util.Objects.requireNonNull;
import static l.files.provider.bookmarks.BookmarkManager.BookmarkChangedListener;

final class BookmarksLoader extends AsyncTaskLoader<List<Path>> {

    private final BookmarkManager manager;
    private final BookmarkChangedListener listener;
    private List<Path> bookmarks;

    BookmarksLoader(Context context, BookmarkManager manager) {
        super(context);
        this.manager = requireNonNull(manager);
        this.listener = new BookmarkListener();
    }

    @Override
    public List<Path> loadInBackground() {
        final Collator collator = Collator.getInstance();
        final List<Path> paths = new ArrayList<>(Collections2.transform(manager.getBookmarks(), new Function<Resource, Path>() {
            @Override
            public Path apply(Resource input) {
                return input.getPath();
            }
        }));
        Collections.sort(paths, new Comparator<Path>() {
            @Override
            public int compare(Path a, Path b) {
                return collator.compare(a.getName(), b.getName());
            }
        });
        return paths;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (bookmarks == null) {
            manager.registerBookmarkChangedListener(listener);
            forceLoad();
        } else {
            deliverResult(bookmarks);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        manager.unregisterBookmarkChangedListener(listener);
    }

    @Override
    public void deliverResult(List<Path> data) {
        super.deliverResult(data);
        this.bookmarks = data;
    }

    private final class BookmarkListener implements BookmarkChangedListener {
        @Override
        public void onBookmarkChanged(BookmarkManager manager) {
            forceLoad();
        }
    }
}
