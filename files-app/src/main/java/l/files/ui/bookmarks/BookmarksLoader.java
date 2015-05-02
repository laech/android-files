package l.files.ui.bookmarks;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.fs.Resource;
import l.files.provider.bookmarks.BookmarkManager;

import static java.util.Objects.requireNonNull;
import static l.files.provider.bookmarks.BookmarkManager.BookmarkChangedListener;

final class BookmarksLoader extends AsyncTaskLoader<List<Resource>> {

    private final BookmarkManager manager;
    private final BookmarkChangedListener listener;
    private List<Resource> bookmarks;

    BookmarksLoader(Context context, BookmarkManager manager) {
        super(context);
        this.manager = requireNonNull(manager);
        this.listener = new BookmarkListener();
    }

    @Override
    public List<Resource> loadInBackground() {
        final Collator collator = Collator.getInstance();
        final List<Resource> resources = new ArrayList<>(manager.getBookmarks());
        Collections.sort(resources, new Comparator<Resource>() {
            @Override
            public int compare(Resource a, Resource b) {
                return collator.compare(a.name(), b.name());
            }
        });
        return resources;
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
    public void deliverResult(List<Resource> data) {
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
