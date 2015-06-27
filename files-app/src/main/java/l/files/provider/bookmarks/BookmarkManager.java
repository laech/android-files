package l.files.provider.bookmarks;

import java.util.Collection;
import java.util.Set;

import l.files.fs.Resource;

public interface BookmarkManager
{

    void addBookmark(Resource resource);

    void removeBookmark(Resource resource);

    void removeBookmarks(Collection<Resource> bookmarks);

    boolean hasBookmark(Resource resource);

    Set<Resource> getBookmarks();

    /**
     * Adds a listener for listening bookmark changes. Does nothing if the
     * listener is already added.
     */
    void registerBookmarkChangedListener(BookmarkChangedListener listener);

    /**
     * Removes a listener from listening bookmark changes. Does nothing if the
     * listener is not already added.
     */
    void unregisterBookmarkChangedListener(BookmarkChangedListener listener);

    interface BookmarkChangedListener
    {
        /**
         * Called when bookmarks has been added/removed. This maybe called from
         * different threads, including the main thread.
         */
        void onBookmarkChanged(BookmarkManager manager);
    }

}
