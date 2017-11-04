package l.files.ui.bookmarks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.Nullable;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.Path;
import l.files.ui.base.text.CollationKey;

import static l.files.base.Objects.requireNonNull;
import static l.files.bookmarks.BookmarkManager.BookmarkChangedListener;

final class BookmarksLoader extends AsyncTaskLoader<List<Path>> {

    private final Path home;
    private final BookmarkManager manager;
    private final BookmarkChangedListener listener;

    @Nullable
    private List<Path> bookmarks;

    BookmarksLoader(Context context, BookmarkManager manager, Path home) {
        super(context);
        this.home = requireNonNull(home);
        this.manager = requireNonNull(manager);
        this.listener = new BookmarkListener();
    }

    @Override
    public List<Path> loadInBackground() {
        Collator collator = Collator.getInstance();
        List<Entry> collation = collateBookmarks(collator);
        List<Path> bookmarks = new ArrayList<>(collation.size());
        for (Entry pair : collation) {
            bookmarks.add(pair.bookmark);
        }
        return bookmarks;
    }

    @NonNull
    private List<Entry> collateBookmarks(Collator collator) {
        List<Entry> collation = new ArrayList<>();
        for (Path bookmark : manager.getBookmarks()) {
            collation.add(new Entry(bookmark, CollationKey.create(collator, bookmark.getName().or(""))));
        }
        Collections.sort(collation, (a, b) -> {
            if (a.bookmark.equals(home)) {
                return -1;
            }
            if (b.bookmark.equals(home)) {
                return 1;
            }
            return a.collationKey.compareTo(b.collationKey);
        });
        return collation;
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

    private static final class Entry {

        final Path bookmark;
        final CollationKey collationKey;

        Entry(Path bookmark, CollationKey collationKey) {
            this.bookmark = bookmark;
            this.collationKey = collationKey;
        }

    }

}
