package l.files.bookmarks;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collection;
import java.util.Set;

import android.support.annotation.Nullable;

import l.files.fs.Path;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public abstract class BookmarkManager {

    // TODO use AbsolutePath

    @Nullable
    private static BookmarkManager instance;

    public static BookmarkManager get(Context context) {
        synchronized (BookmarkManagerImpl.class) {
            if (instance == null) {
                SharedPreferences pref = getDefaultSharedPreferences(context);
                instance = new BookmarkManagerImpl(pref);
            }
            return instance;
        }
    }

    public abstract void addBookmark(Path path);

    public abstract void removeBookmark(Path path);

    public abstract void removeBookmarks(Collection<Path> bookmarks);

    public abstract boolean hasBookmark(Path path);

    public abstract Set<Path> getBookmarks();

    /**
     * Adds a listener for listening bookmark changes. Does nothing if the
     * listener is already added.
     */
    public abstract void registerBookmarkChangedListener(BookmarkChangedListener listener);

    /**
     * Removes a listener from listening bookmark changes. Does nothing if the
     * listener is not already added.
     */
    public abstract void unregisterBookmarkChangedListener(BookmarkChangedListener listener);

    public interface BookmarkChangedListener {
        /**
         * Called when bookmarks has been added/removed. This maybe called from
         * different threads, including the main thread.
         */
        void onBookmarkChanged(BookmarkManager manager);
    }

}
