package l.files.bookmarks;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collection;
import java.util.Set;

import l.files.fs.File;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public abstract class BookmarkManager {

    private static BookmarkManager instance;

    public static BookmarkManager get(final Context context) {
        synchronized (BookmarkManagerImpl.class) {
            if (instance == null) {
                SharedPreferences pref = getDefaultSharedPreferences(context);
                instance = new BookmarkManagerImpl(pref);
            }
            return instance;
        }
    }

    public abstract void addBookmark(File file);

    public abstract void removeBookmark(File file);

    public abstract void removeBookmarks(Collection<File> bookmarks);

    public abstract boolean hasBookmark(File file);

    public abstract Set<File> getBookmarks();

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