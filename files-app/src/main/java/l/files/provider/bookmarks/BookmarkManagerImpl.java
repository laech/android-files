package l.files.provider.bookmarks;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.fs.Resource;
import l.files.fs.local.LocalResource;
import l.files.logging.Logger;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class BookmarkManagerImpl implements BookmarkManager {

  private static BookmarkManagerImpl instance;

  public static BookmarkManagerImpl get(final Context context) {
    synchronized (BookmarkManagerImpl.class) {
      if (instance == null) {
        final SharedPreferences pref = getDefaultSharedPreferences(context);
        instance = new BookmarkManagerImpl(pref);
      }
      return instance;
    }
  }

  private static final Logger logger = Logger.get(BookmarkManagerImpl.class);

  private static final String PREF_KEY = "bookmarks";

  private static final Set<String> DEFAULTS = buildDefaults();

  private static Set<String> buildDefaults() {
    Set<String> defaults = new HashSet<>();
    defaults.add(getExternalStorageDirectory().toURI().toString());
    defaults.add(uri(DIRECTORY_DCIM));
    defaults.add(uri(DIRECTORY_MUSIC));
    defaults.add(uri(DIRECTORY_MOVIES));
    defaults.add(uri(DIRECTORY_PICTURES));
    defaults.add(uri(DIRECTORY_DOWNLOADS));
    return unmodifiableSet(defaults);
  }

  private static String uri(String name) {
    return new File(getExternalStorageDirectory(), name).toURI().toString();
  }

  private final Set<Resource> bookmarks;
  private final SharedPreferences pref;
  private final Set<BookmarkChangedListener> listeners;

  public BookmarkManagerImpl(SharedPreferences pref) {
    this.pref = requireNonNull(pref);
    this.listeners = new CopyOnWriteArraySet<>();
    this.bookmarks = new CopyOnWriteArraySet<>();
  }

  private Set<Resource> toPaths(Set<String> uriStrings) {
    Set<Resource> paths = new HashSet<>();
    for (String uriString : uriStrings) {
      try {
        Resource resource = LocalResource.create(new File(new URI(uriString)));
        try {
          if (resource.exists(NOFOLLOW)) {
            paths.add(resource);
          }
        } catch (IOException ignored) {
          // Remove bookmarks that no longer exist
        }
      } catch (URISyntaxException | IllegalArgumentException e) {
        logger.warn(e, "Ignoring bookmark string  \"%s\"", uriString);
      }
    }
    return paths;
  }

  @Override public void addBookmark(Resource resource) {
    requireNonNull(resource, "resource");
    if (bookmarks.add(resource)) {
      saveBookmarksAndNotify();
    }
  }

  @Override public void removeBookmark(Resource resource) {
    requireNonNull(resource, "resource");
    if (bookmarks.remove(resource)) {
      saveBookmarksAndNotify();
    }
  }

  @Override public void removeBookmarks(Collection<Resource> bookmarks) {
    requireNonNull(bookmarks, "bookmarks");
    if (this.bookmarks.removeAll(bookmarks)) {
      saveBookmarksAndNotify();
    }
  }

  private void saveBookmarksAndNotify() {
    pref.edit().putStringSet(PREF_KEY, toUriStrings(bookmarks)).apply();
    notifyListeners();
  }

  private Set<String> toUriStrings(Set<? extends Resource> bookmarks) {
    Set<String> uris = new HashSet<>();
    for (Resource bookmark : bookmarks) {
      uris.add(bookmark.uri().toString());
    }
    return uris;
  }

  private void notifyListeners() {
    for (BookmarkChangedListener listener : listeners) {
      listener.onBookmarkChanged(this);
    }
  }

  @Override public boolean hasBookmark(Resource resource) {
    return bookmarks.contains(resource);
  }

  @Override public Set<Resource> getBookmarks() {
    synchronized (this) {
      if (bookmarks.isEmpty()) {
        bookmarks.addAll(loadBookmarks());
      }
    }
    return unmodifiableSet(new HashSet<>(bookmarks));
  }

  public Set<Resource> loadBookmarks() {
    return toPaths(pref.getStringSet(PREF_KEY, DEFAULTS));
  }

  @Override public void registerBookmarkChangedListener(
      BookmarkChangedListener listener) {
    requireNonNull(listener);
    listeners.add(listener);
  }

  @Override public void unregisterBookmarkChangedListener(
      BookmarkChangedListener listener) {
    requireNonNull(listener);
    listeners.remove(listener);
  }
}
