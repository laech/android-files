package l.files.ui.bookmarks;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import l.files.fs.File;
import l.files.fs.FileName;
import l.files.bookmarks.BookmarkManager;

import static java.util.Objects.requireNonNull;
import static l.files.bookmarks.BookmarkManager.BookmarkChangedListener;

final class BookmarksLoader extends AsyncTaskLoader<List<File>> {

    private final BookmarkManager manager;
    private final BookmarkChangedListener listener;
    private List<File> bookmarks;

    BookmarksLoader(final Context context, final BookmarkManager manager) {
        super(context);
        this.manager = requireNonNull(manager);
        this.listener = new BookmarkListener();
    }

    @Override
    public List<File> loadInBackground() {
        final Comparator<FileName> comparator = FileName.comparator(Locale.getDefault());
        final List<File> files = new ArrayList<>(manager.getBookmarks());
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(final File a, final File b) {
                return comparator.compare(a.name(), b.name());
            }
        });
        return files;
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
    public void deliverResult(final List<File> data) {
        super.deliverResult(data);
        this.bookmarks = data;
    }

    private final class BookmarkListener implements BookmarkChangedListener {
        @Override
        public void onBookmarkChanged(final BookmarkManager manager) {
            forceLoad();
        }
    }
}
