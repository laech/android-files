package l.files.provider.bookmarks;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
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
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class BookmarkManagerImpl implements BookmarkManager
{

    private static BookmarkManagerImpl instance;

    public static BookmarkManagerImpl get(final Context context)
    {
        synchronized (BookmarkManagerImpl.class)
        {
            if (instance == null)
            {
                final SharedPreferences pref = getDefaultSharedPreferences(context);
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

    private static String uri(final String name)
    {
        return new File(getExternalStorageDirectory(), name).toURI().toString();
    }

    private final ResourceProvider provider;
    private final Set<Resource> bookmarks;
    private final SharedPreferences pref;
    private final Set<BookmarkChangedListener> listeners;

    @VisibleForTesting
    public BookmarkManagerImpl(
            final ResourceProvider provider,
            final SharedPreferences pref)
    {
        this.provider = requireNonNull(provider);
        this.pref = requireNonNull(pref);
        this.listeners = new CopyOnWriteArraySet<>();
        this.bookmarks = new CopyOnWriteArraySet<>();
    }

    private Set<Resource> toPaths(final Set<String> uriStrings)
    {
        final Set<Resource> paths = newHashSetWithExpectedSize(uriStrings.size());
        for (final String uriString : uriStrings)
        {
            try
            {
                final URI uri = new URI(uriString);
                final Resource resource = provider.get(uri);
                try
                {
                    if (resource.exists(NOFOLLOW))
                    {
                        paths.add(resource);
                    }
                }
                catch (final IOException ignored)
                {
                    // Remove bookmarks that no longer exist
                }
            }
            catch (URISyntaxException | IllegalArgumentException e)
            {
                logger.warn(e, "Ignoring bookmark string  \"%s\"", uriString);
            }
        }
        return paths;
    }

    @Override
    public void addBookmark(final Resource resource)
    {
        requireNonNull(resource, "resource");
        if (bookmarks.add(resource))
        {
            saveBookmarksAndNotify();
        }
    }

    @Override
    public void removeBookmark(final Resource resource)
    {
        requireNonNull(resource, "resource");
        if (bookmarks.remove(resource))
        {
            saveBookmarksAndNotify();
        }
    }

    @Override
    public void removeBookmarks(final Collection<Resource> bookmarks)
    {
        requireNonNull(bookmarks, "bookmarks");
        if (this.bookmarks.removeAll(bookmarks))
        {
            saveBookmarksAndNotify();
        }
    }

    private void saveBookmarksAndNotify()
    {
        pref.edit().putStringSet(PREF_KEY, toUriStrings(bookmarks)).apply();
        notifyListeners();
    }

    private Set<String> toUriStrings(final Set<? extends Resource> bookmarks)
    {
        return new HashSet<>(transform(bookmarks, new Function<Resource, String>()
        {
            @Override
            public String apply(final Resource input)
            {
                return input.uri().toString();
            }
        }));
    }

    private void notifyListeners()
    {
        for (final BookmarkChangedListener listener : listeners)
        {
            listener.onBookmarkChanged(this);
        }
    }

    @Override
    public boolean hasBookmark(final Resource resource)
    {
        return bookmarks.contains(resource);
    }

    @Override
    public Set<Resource> getBookmarks()
    {
        synchronized (this)
        {
            if (bookmarks.isEmpty())
            {
                bookmarks.addAll(loadBookmarks());
            }
        }
        return ImmutableSet.copyOf(bookmarks);
    }

    @VisibleForTesting
    public Set<Resource> loadBookmarks()
    {
        return toPaths(pref.getStringSet(PREF_KEY, DEFAULTS));
    }

    @Override
    public void registerBookmarkChangedListener(
            final BookmarkChangedListener listener)
    {
        requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void unregisterBookmarkChangedListener(
            final BookmarkChangedListener listener)
    {
        requireNonNull(listener);
        listeners.remove(listener);
    }
}
