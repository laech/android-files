package l.files.bookmarks;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.fs.Path;
import l.files.fs.local.LocalPath;

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

    private static final String TAG = BookmarkManagerImpl.class.getSimpleName();

    private static final String PREF_KEY_V1 = "bookmarks";
    private static final String PREF_KEY_V2 = "bookmarks2";

    private Set<Path> createDefaultBookmarks() {
        Set<Path> defaults = new HashSet<>();
        addIfExists(defaults, LocalPath.fromFile(getExternalStorageDirectory()));
        addIfExists(defaults, externalStoragePath(DIRECTORY_DCIM));
        addIfExists(defaults, externalStoragePath(DIRECTORY_MUSIC));
        addIfExists(defaults, externalStoragePath(DIRECTORY_MOVIES));
        addIfExists(defaults, externalStoragePath(DIRECTORY_PICTURES));
        addIfExists(defaults, externalStoragePath(DIRECTORY_DOWNLOADS));
        addIfExists(defaults, LocalPath.fromString("/sdcard2"));
        return unmodifiableSet(defaults);
    }

    private void addIfExists(Set<Path> paths, Path path) {
        try {
            if (exists(path)) {
                paths.add(path);
            }
        } catch (IOException ignored) {
        }
    }

    private boolean exists(Path path) throws IOException {
        return path.exists(FOLLOW);
    }

    private static Path externalStoragePath(String name) {
        return LocalPath.fromFile(new File(getExternalStorageDirectory(), name));
    }

    private final Set<Path> bookmarks;
    private final SharedPreferences pref;
    private final Set<BookmarkChangedListener> listeners;

    public BookmarkManagerImpl(SharedPreferences pref) {
        this.pref = requireNonNull(pref);
        this.listeners = new CopyOnWriteArraySet<>();
        this.bookmarks = new CopyOnWriteArraySet<>();
    }

    private Set<Path> toPathsV1(Set<String> uriStrings) {
        Set<Path> paths = new HashSet<>();
        for (String uriString : uriStrings) {
            try {
                Path path = LocalPath.fromFile(new File(new URI(uriString)));
                try {
                    if (exists(path)) {
                        paths.add(path);
                    }
                } catch (IOException ignored) {
                    // Remove bookmarks that no longer exist
                }
            } catch (URISyntaxException | IllegalArgumentException e) {
                Log.w(TAG, "Invalid URI: " + uriString, e);
            }
        }
        return paths;
    }


    private static Set<String> encode(Collection<? extends Path> bookmarks) {
        Set<String> encoded = new HashSet<>();
        for (Path path : bookmarks) {
            encoded.add(encode(path));
        }
        return unmodifiableSet(encoded);
    }

    private static String encode(Path path) {
        return ":" // For backward compatibility
                + Base64.encodeToString(path.toByteArray(), Base64.DEFAULT);
    }

    private static Path decode(String encoded) {
        String[] parts = encoded.split(":");
        if (parts.length == 2) {
            return LocalPath.fromByteArray(Base64.decode(parts[1], Base64.DEFAULT));
        } else {
            throw new IllegalArgumentException("Invalid bookmark: " + encoded);
        }
    }

    private Set<Path> decode(Collection<String> encoded) {
        Set<Path> bookmarks = new HashSet<>();
        for (String element : encoded) {
            try {
                Path path = decode(element);
                if (exists(path)) {
                    bookmarks.add(path);
                }
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Invalid bookmark: " + element, e);
            } catch (IOException e) {
                // TODO should bookmarks that don't exist be removed or kept?
            }
        }
        return unmodifiableSet(bookmarks);
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
        pref.edit().putStringSet(PREF_KEY_V2, encode(bookmarks)).apply();
        notifyListeners();
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

    Set<Path> loadBookmarks() {
        Set<String> encodedPaths = pref.getStringSet(PREF_KEY_V2, null);
        if (encodedPaths != null) {
            return decode(encodedPaths);
        }
        Set<String> pathsV1 = pref.getStringSet(PREF_KEY_V1, null);
        if (pathsV1 != null) {
            return toPathsV1(pathsV1);
        }
        return createDefaultBookmarks();
    }

    @Override
    public void registerBookmarkChangedListener(
            BookmarkChangedListener listener
    ) {
        requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void unregisterBookmarkChangedListener(
            BookmarkChangedListener listener
    ) {
        requireNonNull(listener);
        listeners.remove(listener);
    }
}
