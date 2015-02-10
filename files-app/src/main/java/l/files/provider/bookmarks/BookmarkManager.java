package l.files.provider.bookmarks;

import java.util.Collection;
import java.util.Set;

import l.files.fs.Path;

public interface BookmarkManager {

  void addBookmark(Path path);

  void removeBookmark(Path path);

  void removeBookmarks(Collection<Path> bookmarks);

  boolean hasBookmark(Path path);

  Set<Path> getBookmarks();

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

  static interface BookmarkChangedListener {
    /**
     * Called when bookmarks has been added/removed. This maybe called from
     * different threads, including the main thread.
     */
    void onBookmarkChanged(BookmarkManager manager);

  }
}
