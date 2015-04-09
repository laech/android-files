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

import l.files.fs.DefaultResourceProvider;
import l.files.fs.Resource;
import l.files.fs.ResourceProvider;
import l.files.logging.Logger;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.collect.Collections2.transform;
import static java.util.Objects.requireNonNull;

public final class BookmarkManagerImpl implements BookmarkManager {

    private static BookmarkManagerImpl instance;

    public static BookmarkManagerImpl get(Context context) {
        synchronized (BookmarkManagerImpl.class) {
            if (instance == null) {
                SharedPreferences pref = getDefaultSharedPreferences(context);
                instance = new BookmarkManagerImpl(DefaultResourceProvider.INSTANCE, pref);
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

    private final ResourceProvider provider;
    private final Set<Resource> bookmarks;
    private final SharedPreferences pref;
    private final Set<BookmarkChangedListener> listeners;

    @VisibleForTesting
    public BookmarkManagerImpl(ResourceProvider provider, SharedPreferences pref) {
        this.provider = requireNonNull(provider);
        this.pref = requireNonNull(pref);
        this.listeners = new CopyOnWriteArraySet<>();
        this.bookmarks = new CopyOnWriteArraySet<>();
    }

    private Set<Resource> toPaths(Set<String> uriStrings) {
        Set<Resource> paths = Sets.newHashSetWithExpectedSize(uriStrings.size());
        for (String uriString : uriStrings) {
            try {
                URI uri = new URI(uriString);
                Resource resource = provider.get(uri);
                paths.add(resource);
            } catch (URISyntaxException | IllegalArgumentException e) {
                logger.warn(e, "Ignoring bookmark string  \"%s\"", uriString);
            }
        }
        return paths;
    }

    @Override
    public void addBookmark(Resource resource) {
        requireNonNull(resource, "resource");
        if (bookmarks.add(resource)) {
            saveBookmarksAndNotify();
        }
    }

    @Override
    public void removeBookmark(Resource resource) {
        requireNonNull(resource, "resource");
        if (bookmarks.remove(resource)) {
            saveBookmarksAndNotify();
        }
    }

    @Override
    public void removeBookmarks(Collection<Resource> bookmarks) {
        requireNonNull(bookmarks, "bookmarks");
        if (this.bookmarks.removeAll(bookmarks)) {
            saveBookmarksAndNotify();
        }
    }

    @VisibleForTesting
    public boolean clearBookmarksSync() {
        bookmarks.clear();
        return pref.edit().putStringSet(PREF_KEY, toUriStrings(bookmarks)).commit();
    }

    private void saveBookmarksAndNotify() {
        pref.edit().putStringSet(PREF_KEY, toUriStrings(bookmarks)).apply();
        notifyListeners();
    }

    private Set<String> toUriStrings(Set<? extends Resource> bookmarks) {
        return new HashSet<>(transform(bookmarks, new Function<Resource, String>() {
            @Override
            public String apply(Resource input) {
                return input.getUri().toString();
            }
        }));
    }

    private void notifyListeners() {
        for (BookmarkChangedListener listener : listeners) {
            listener.onBookmarkChanged(this);
        }
    }

    @Override
    public boolean hasBookmark(Resource resource) {
        return bookmarks.contains(resource);
    }

    @Override
    public Set<Resource> getBookmarks() {
        synchronized (this) {
            if (bookmarks.isEmpty()) {
                bookmarks.addAll(toPaths(pref.getStringSet(PREF_KEY, DEFAULTS)));
            }
        }
        return ImmutableSet.copyOf(bookmarks);
    }

    @Override
    public void registerBookmarkChangedListener(BookmarkChangedListener listener) {
        requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void unregisterBookmarkChangedListener(BookmarkChangedListener listener) {
        requireNonNull(listener);
        listeners.remove(listener);
    }
}
