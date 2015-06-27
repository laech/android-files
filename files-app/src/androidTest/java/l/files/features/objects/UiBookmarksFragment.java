package l.files.features.objects;

import android.app.Instrumentation;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.Resource;
import l.files.ui.bookmarks.BookmarksFragment;
import l.files.ui.browser.FilesActivity;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;
import static l.files.features.objects.Instrumentations.clickItemOnMainThread;
import static l.files.features.objects.Instrumentations.findItemOnMainThread;
import static l.files.features.objects.Instrumentations.longClickItemOnMainThread;

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

    public UiBookmarksFragment longClick(final Resource bookmark)
    {
        longClickItemOnMainThread(instrument, fragment.recycler, bookmark);
        return this;
    }

    public UiBookmarksFragment click(final Resource bookmark)
    {
        clickItemOnMainThread(instrument, fragment.recycler, bookmark);
        return this;
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

    public UiBookmarksFragment assertActionModePresent(final boolean present)
    {
        activityObject().assertActionModePresent(present);
        return this;
    }

    public UiBookmarksFragment assertActionModeTitle(final Object title)
    {
        activityObject().assertActionModeTitle(title);
        return this;
    }

    public UiBookmarksFragment rotate()
    {
        activityObject().rotate();
        return this;
    }

    public UiBookmarksFragment assertChecked(
            final Resource bookmark,
            final boolean checked)
    {
        findItemOnMainThread(
                instrument, fragment.recycler, bookmark, new Consumer<View>()
                {
                    @Override
                    public void apply(final View view)
                    {
                        assertEquals(checked, view.isActivated());
                    }
                });
        return this;
    }

    public UiBookmarksFragment assertDrawerIsOpened(final boolean opened)
    {
        activityObject().assertDrawerIsOpened(opened);
        return this;
    }

    public UiBookmarksFragment pressBack()
    {
        activityObject().pressBack();
        return this;
    }
}
