package l.files.provider.bookmarks;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.fs.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.provider.bookmarks.BookmarkManager.BookmarkChangedListener;

public final class BookmarksLoader extends AsyncTaskLoader<List<Path>> {

  private final BookmarkManager manager;
  private final BookmarkChangedListener listener;
  private List<Path> bookmarks;

  public BookmarksLoader(Context context, BookmarkManager manager) {
    super(context);
    this.manager = checkNotNull(manager);
    this.listener = new BookmarkListener();
  }

  @Override public List<Path> loadInBackground() {
    final Collator collator = Collator.getInstance();
    final List<Path> paths = new ArrayList<>(manager.getBookmarks());
    Collections.sort(paths, new Comparator<Path>() {
      @Override public int compare(Path a, Path b) {
        return collator.compare(a.getName(), b.getName());
      }
    });
    return paths;
  }

  @Override protected void onStartLoading() {
    super.onStartLoading();
    if (bookmarks == null) {
      manager.registerBookmarkChangedListener(listener);
      forceLoad();
    } else {
      deliverResult(bookmarks);
    }
  }

  @Override protected void onReset() {
    super.onReset();
    manager.unregisterBookmarkChangedListener(listener);
  }

  @Override public void deliverResult(List<Path> data) {
    super.deliverResult(data);
    this.bookmarks = data;
  }

  private final class BookmarkListener implements BookmarkChangedListener {
    @Override public void onBookmarkChanged(BookmarkManager manager) {
      forceLoad();
    }
  }
}
