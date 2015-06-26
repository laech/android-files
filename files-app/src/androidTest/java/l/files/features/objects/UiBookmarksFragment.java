package l.files.features.objects;

import android.app.Instrumentation;

import java.util.ArrayList;
import java.util.List;

import l.files.R;
import l.files.features.objects.action.Action;
import l.files.features.objects.action.StableRecyclerViewAction;
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

    private FilesActivity activity()
    {
        return (FilesActivity) fragment.getActivity();
    }

    public UiFileActivity activityObject()
    {
        return new UiFileActivity(instrument, activity());
    }

    private Action<Boolean> clicker()
    {
        return StableRecyclerViewAction.willClick(fragment.recycler);
    }

    public UiBookmarksFragment toggleSelection(final Resource bookmark)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                fragment.selection().toggle(bookmark);
            }
        });
        return this;
    }

    public UiFileActivity click(final Resource bookmark)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                clicker().action(bookmark);
            }
        });
        return activityObject();
    }

    public UiBookmarksFragment delete()
    {
        final UiFileActivity activity = activityObject();
        activity.selectActionModeAction(R.id.delete_selected_bookmarks);
        activity.waitForActionModeToFinish();
        return this;
    }

    public UiBookmarksFragment assertCurrentDirectoryBookmarked(
            final boolean bookmarked)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final Resource dir = activity().fragment().directory();
                final List<Resource> all = fragment.bookmarks();
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
                assertEquals(bookmarked, fragment.bookmarks().contains(bookmark));
            }
        });
        return this;
    }

    public UiBookmarksFragment assertContainsBookmarksInOrder(
            final Resource... bookmarks)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final List<Resource> expected = asList(bookmarks);
                final List<Resource> actual = new ArrayList<>();
                for (final Resource bookmark : fragment.bookmarks())
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
}
