package l.files.provider.bookmarks;

import android.content.SharedPreferences;

import com.google.android.gms.common.util.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.fs.FileSystem;
import l.files.fs.FileSystemException;
import l.files.fs.Path;
import l.files.logging.Logger;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;
import static l.files.provider.FilesContract.getFileId;

final class BookmarkManagerImpl implements BookmarkManager {

  private static final Logger logger = Logger.get(BookmarkManagerImpl.class);

  private static final String PREF_KEY = "bookmarks";

  private static final Set<String> DEFAULTS = ImmutableSet.<String>builder()
      .add(getFileId(getExternalStorageDirectory()))
      .add(uri(DIRECTORY_DCIM))
      .add(uri(DIRECTORY_MUSIC))
      .add(uri(DIRECTORY_MOVIES))
      .add(uri(DIRECTORY_PICTURES))
      .add(uri(DIRECTORY_DOWNLOADS))
      .build();

  private final FileSystem fs;
  private final Set<Path> bookmarks;
  private final SharedPreferences pref;
  private final Set<BookmarkChangedListener> listeners;

  BookmarkManagerImpl(FileSystem fs, SharedPreferences pref) {
    this.fs = checkNotNull(fs, "fs");
    this.pref = checkNotNull(pref, "pref");
    this.listeners = new CopyOnWriteArraySet<>();
    this.bookmarks = new CopyOnWriteArraySet<>();
  }

  private Set<Path> toPaths(Set<String> uriStrings) {
    Set<Path> paths = Sets.newHashSetWithExpectedSize(uriStrings.size());
    for (String uriString : uriStrings) {
      try {
        URI uri = new URI(uriString);
        Path path = fs.path(uri);
        paths.add(path);
      } catch (URISyntaxException | FileSystemException e) {
        logger.warn(e, "Ignoring bookmark string  \"%s\"", uriString);
      }
    }
    return paths;
  }

  private static String uri(String name) {
    return new File(getExternalStorageDirectory(), name).toURI().toString();
  }

  @Override public void addBookmark(Path path) {
    checkNotNull(path);
    if (bookmarks.add(path)) {
      saveBookmarksAndNotify();
    }
  }

  @Override public void removeBookmark(Path path) {
    checkNotNull(path);
    if (bookmarks.remove(path)) {
      saveBookmarksAndNotify();
    }
  }

  @VisibleForTesting boolean clearBookmarksSync() {
    bookmarks.clear();
    return pref.edit().putStringSet(PREF_KEY, toUriStrings(bookmarks)).commit();
  }

  private void saveBookmarksAndNotify() {
    pref.edit().putStringSet(PREF_KEY, toUriStrings(bookmarks)).apply();
    notifyListeners();
  }

  private Set<String> toUriStrings(Set<Path> bookmarks) {
    return new HashSet<>(transform(bookmarks, new Function<Path, String>() {
      @Override public String apply(Path input) {
        return input.uri().toString();
      }
    }));
  }

  private void notifyListeners() {
    for (BookmarkChangedListener listener : listeners) {
      listener.onBookmarkChanged(this);
    }
  }

  @Override public boolean hasBookmark(Path path) {
    return bookmarks.contains(path);
  }

  @Override public Set<Path> getBookmarks() {
    synchronized (this) {
      if (bookmarks.isEmpty()) {
        bookmarks.addAll(toPaths(pref.getStringSet(PREF_KEY, DEFAULTS)));
      }
    }
    return ImmutableSet.copyOf(bookmarks);
  }

  @Override
  public void registerBookmarkChangedListener(BookmarkChangedListener listener) {
    checkNotNull(listener);
    listeners.add(listener);
  }

  @Override
  public void unregisterBookmarkChangedListener(BookmarkChangedListener listener) {
    checkNotNull(listener);
    listeners.remove(listener);
  }
}