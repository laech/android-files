package l.files.bookmarks;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Paths;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static java.util.Collections.unmodifiableSet;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;

final class BookmarkManagerImpl extends BookmarkManager {

    // TODO don't use URI string any, use bytes

    private static final String PREF_KEY = "bookmarks";

    private static final Set<String> DEFAULTS = buildDefaults();

    @SuppressLint("SdCardPath")
    private static Set<String> buildDefaults() {
        Set<String> defaults = new HashSet<>();
        defaults.add(getExternalStorageDirectory().toURI().toString());
        defaults.add(uri(DIRECTORY_DCIM));
        defaults.add(uri(DIRECTORY_MUSIC));
        defaults.add(uri(DIRECTORY_MOVIES));
        defaults.add(uri(DIRECTORY_PICTURES));
        defaults.add(uri(DIRECTORY_DOWNLOADS));
        defaults.add(new java.io.File("/sdcard2").toURI().toString());
        return unmodifiableSet(defaults);
    }

    private static String uri(String name) {
        return new java.io.File(getExternalStorageDirectory(), name).toURI().toString();
    }

    private final Set<Path> bookmarks;
    private final SharedPreferences pref;
    private final Set<BookmarkChangedListener> listeners;

    public BookmarkManagerImpl(SharedPreferences pref) {
        this.pref = requireNonNull(pref);
        this.listeners = new CopyOnWriteArraySet<>();
        this.bookmarks = new CopyOnWriteArraySet<>();
    }

    private Set<Path> toPaths(Set<String> uriStrings) {
        Set<Path> paths = new HashSet<>();
        for (String uriString : uriStrings) {
            try {
                Path path = Paths.get(new URI(uriString));
                try {
                    if (Files.exists(path, FOLLOW)) {
                        paths.add(path);
                    }
                } catch (IOException ignored) {
                    // Remove bookmarks that no longer exist
                }
            } catch (URISyntaxException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return paths;
    }

    @Override
    public void addBookmark(Path path) {
        requireNonNull(path, "path");
        if (bookmarks.add(path)) {
            saveBookmarksAndNotify();
        }
    }

    @Override
    public void removeBookmark(Path path) {
        requireNonNull(path, "path");
        if (bookmarks.remove(path)) {
            saveBookmarksAndNotify();
        }
    }

    @Override
    public void removeBookmarks(Collection<Path> bookmarks) {
        requireNonNull(bookmarks, "bookmarks");
        if (this.bookmarks.removeAll(bookmarks)) {
            saveBookmarksAndNotify();
        }
    }

    private void saveBookmarksAndNotify() {
        pref.edit().putStringSet(PREF_KEY, toUriStrings(bookmarks)).apply();
        notifyListeners();
    }

    private Set<String> toUriStrings(Set<? extends Path> bookmarks) {
        Set<String> uris = new HashSet<>();
        for (Path bookmark : bookmarks) {
            uris.add(bookmark.toUri().toString());
        }
        return uris;
    }

    private void notifyListeners() {
        for (BookmarkChangedListener listener : listeners) {
            listener.onBookmarkChanged(this);
        }
    }

    @Override
    public boolean hasBookmark(Path path) {
        return bookmarks.contains(path);
    }

    @Override
    public Set<Path> getBookmarks() {
        synchronized (this) {
            if (bookmarks.isEmpty()) {
                bookmarks.addAll(loadBookmarks());
            }
        }
        return unmodifiableSet(new HashSet<>(bookmarks));
    }

    public Set<Path> loadBookmarks() {
        return toPaths(pref.getStringSet(PREF_KEY, DEFAULTS));
    }

    @Override
    public void registerBookmarkChangedListener(
            BookmarkChangedListener listener) {
        requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void unregisterBookmarkChangedListener(
            BookmarkChangedListener listener) {
        requireNonNull(listener);
        listeners.remove(listener);
    }
}
