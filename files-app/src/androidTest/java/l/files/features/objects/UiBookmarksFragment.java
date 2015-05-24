package l.files.features.objects;

import android.app.Instrumentation;
import android.widget.ListView;

import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;

import l.files.R;
import l.files.fs.Resource;
import l.files.ui.bookmarks.BookmarksFragment;
import l.files.ui.browser.FilesActivity;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;

public final class UiBookmarksFragment
{

    private final Instrumentation instrument;
    private final BookmarksFragment fragment;

    public UiBookmarksFragment(
            final Instrumentation instrument,
            final BookmarksFragment fragment)
    {
        this.instrument = instrument;
        this.fragment = fragment;
    }

    private ListView getListView()
    {
        return (ListView) fragment.getView();
    }

    private FilesActivity getActivity()
    {
        return (FilesActivity) fragment.getActivity();
    }

    public UiFileActivity getActivityObject()
    {
        return new UiFileActivity(instrument, getActivity());
    }

    public UiBookmarksFragment checkBookmark(
            final Resource bookmark,
            final boolean checked)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final int i = indexOf(getListView(), new Predicate<Object>()
                {
                    @Override
                    public boolean apply(final Object input)
                    {
                        return input.equals(bookmark);
                    }
                });
                getListView().setItemChecked(i, checked);
            }
        });
        return this;

    }

    public UiBookmarksFragment deleteCheckedBookmarks()
    {
        final UiFileActivity activity = getActivityObject();
        activity.selectActionModeAction(R.id.delete_selected_bookmarks);
        activity.waitForActionModeToFinish();
        return this;
    }

    private List<Resource> bookmarks()
    {
        final List<Resource> resources = new ArrayList<>();
        for (int i = getListView().getHeaderViewsCount();
             i < getListView().getCount();
             i++)
        {
            resources.add((Resource) getListView().getItemAtPosition(i));
        }
        return resources;
    }

    public UiBookmarksFragment assertCurrentDirectoryBookmarked(
            final boolean bookmarked)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final Resource dir = getActivity().fragment().directory();
                final List<Resource> all = bookmarks();
                assertEquals(all.toString(), bookmarked, all.contains(dir));
            }
        });
        return this;
    }

    public UiBookmarksFragment assertBookmarked(
            final Resource bookmark,
            final boolean bookmarked)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(bookmarked, bookmarks().contains(bookmark));
            }
        });
        return this;
    }

    public UiBookmarksFragment assertContainsBookmarksInOrder(
            final Resource... resources)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final List<Resource> expected = asList(resources);
                final List<Resource> actual = new ArrayList<>();
                for (final Resource bookmark : bookmarks())
                {
                    if (expected.contains(bookmark))
                    {
                        actual.add(bookmark);
                    }
                }
                assertEquals(expected, actual);
            }
        });
        return this;
    }

    private int indexOf(final ListView list, final Predicate<Object> pred)
    {
        for (int i = list.getHeaderViewsCount(); i < list.getCount(); i++)
        {
            if (pred.apply(list.getItemAtPosition(i)))
            {
                return i;
            }
        }
        throw new AssertionError("Not found");
    }

}
