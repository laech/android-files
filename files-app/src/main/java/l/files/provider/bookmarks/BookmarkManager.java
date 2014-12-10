package l.files.provider.bookmarks;

import java.util.Set;

import l.files.fs.Path;

public interface BookmarkManager {

  void addBookmark(Path path);

  void removeBookmark(Path path);

  boolean hasBookmark(Path path);

  Set<Path> getBookmarks();

  void addBookmarkChangedListener(BookmarkChangedListener listener);

  void removeBookmarkChangedListener(BookmarkChangedListener listener);

  static interface BookmarkChangedListener {
    /**
     * Called when bookmarks has been added/removed.
     * This maybe called from different threads, including the main thread.
     */
    void onBookmarkChanged(BookmarkManager manager);

  }
}
