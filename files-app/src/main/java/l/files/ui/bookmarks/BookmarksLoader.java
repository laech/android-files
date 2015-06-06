package l.files.ui.bookmarks;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import l.files.fs.Resource;
import l.files.fs.Resource.Name;
import l.files.provider.bookmarks.BookmarkManager;

import static java.util.Objects.requireNonNull;
import static l.files.provider.bookmarks.BookmarkManager.BookmarkChangedListener;

final class BookmarksLoader extends AsyncTaskLoader<List<Resource>>
{

    private final BookmarkManager manager;
    private final BookmarkChangedListener listener;
    private List<Resource> bookmarks;

    BookmarksLoader(final Context context, final BookmarkManager manager)
    {
        super(context);
        this.manager = requireNonNull(manager);
        this.listener = new BookmarkListener();
    }

    @Override
    public List<Resource> loadInBackground()
    {
        final Comparator<Name> comparator = Name.comparator(Locale.getDefault());
        final List<Resource> resources = new ArrayList<>(manager.getBookmarks());
        Collections.sort(resources, new Comparator<Resource>()
        {
            @Override
            public int compare(final Resource a, final Resource b)
            {
                return comparator.compare(a.name(), b.name());
            }
        });
        return resources;
    }

    @Override
    protected void onStartLoading()
    {
        super.onStartLoading();
        if (bookmarks == null)
        {
            manager.registerBookmarkChangedListener(listener);
            forceLoad();
        }
        else
        {
            deliverResult(bookmarks);
        }
    }

    @Override
    protected void onReset()
    {
        super.onReset();
        manager.unregisterBookmarkChangedListener(listener);
    }

    @Override
    public void deliverResult(final List<Resource> data)
    {
        super.deliverResult(data);
        this.bookmarks = data;
    }

    private final class BookmarkListener implements BookmarkChangedListener
    {
        @Override
        public void onBookmarkChanged(final BookmarkManager manager)
        {
            forceLoad();
        }
    }
}
