package l.files.provider.bookmarks;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.fs.DefaultPathProvider;
import l.files.fs.Path;
import l.files.fs.PathProvider;
import l.files.logging.Logger;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;

public final class BookmarkManagerImpl implements BookmarkManager {

  private static BookmarkManagerImpl instance;

  public static BookmarkManagerImpl get(Context context) {
    synchronized (BookmarkManagerImpl.class) {
      if (instance == null) {
        SharedPreferences pref = getDefaultSharedPreferences(context);
        instance = new BookmarkManagerImpl(DefaultPathProvider.INSTANCE, pref);
      }
      return instance;
    }
  }

  private static final Logger logger = Logger.get(BookmarkManagerImpl.class);

  private static final String PREF_KEY = "bookmarks";

  private static final Set<String> DEFAULTS = ImmutableSet.<String>builder()
      .add(getExternalStorageDirectory().toURI().toString())
      .add(uri(DIRECTORY_DCIM))
      .add(uri(DIRECTORY_MUSIC))
      .add(uri(DIRECTORY_MOVIES))
      .add(uri(DIRECTORY_PICTURES))
      .add(uri(DIRECTORY_DOWNLOADS))
      .build();

  private static String uri(String name) {
    return new File(getExternalStorageDirectory(), name).toURI().toString();
  }

  private final PathProvider provider;
  private final Set<Path> bookmarks;
  private final SharedPreferences pref;
  private final Set<BookmarkChangedListener> listeners;

  @VisibleForTesting
  public BookmarkManagerImpl(PathProvider provider, SharedPreferences pref) {
    this.provider = checkNotNull(provider);
    this.pref = checkNotNull(pref);
    this.listeners = new CopyOnWriteArraySet<>();
    this.bookmarks = new CopyOnWriteArraySet<>();
  }

  private Set<Path> toPaths(Set<String> uriStrings) {
    Set<Path> paths = Sets.newHashSetWithExpectedSize(uriStrings.size());
    for (String uriString : uriStrings) {
      try {
        URI uri = new URI(uriString);
        Path path = provider.get(uri);
        paths.add(path);
      } catch (URISyntaxException | IllegalArgumentException e) {
        logger.warn(e, "Ignoring bookmark string  \"%s\"", uriString);
      }
    }
    return paths;
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

  @Override public void removeBookmarks(Collection<Path> bookmarks) {
    checkNotNull(bookmarks);
    if (this.bookmarks.removeAll(bookmarks)) {
      saveBookmarksAndNotify();
    }
  }

  @VisibleForTesting public boolean clearBookmarksSync() {
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
        return input.getUri().toString();
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
