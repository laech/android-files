package l.files.features.objects;

import android.app.ActionBar;
import android.app.Instrumentation;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Optional;

import java.io.File;
import java.util.List;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.Resource;
import l.files.fs.local.LocalResource;
import l.files.ui.FileLabels;
import l.files.ui.bookmarks.BookmarksFragment;
import l.files.ui.browser.FileListItem;
import l.files.ui.browser.FilesActivity;
import l.files.ui.browser.FilesFragment;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.view.Gravity.START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static l.files.features.objects.Instrumentations.await;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;
import static l.files.test.Mocks.mockMenuItem;

public final class UiFileActivity
{

    private final Instrumentation instrument;
    private final FilesActivity activity;

    public UiFileActivity(
            final Instrumentation instrumentation,
            final FilesActivity activity)
    {
        this.instrument = instrumentation;
        this.activity = activity;
    }

    public UiFileActivity bookmark()
    {
        assertBookmarkMenuChecked(false);
        await(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertTrue(instrument.invokeMenuActionSync(
                        activity, R.id.bookmark, 0));
            }
        });
        return this;
    }

    public UiFileActivity unbookmark()
    {
        assertBookmarkMenuChecked(true);
        await(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertTrue(instrument.invokeMenuActionSync(
                        activity, R.id.bookmark, 0));
            }
        });
        return this;
    }

    public UiFileActivity check(final File file, final boolean checked)
    {
        return check(LocalResource.create(file), checked);
    }

    public UiFileActivity check(final Resource resource, final boolean checked)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                listView().setItemChecked(
                        findItemPositionOrThrow(resource), checked);
            }
        });
        return this;
    }

    public UiNewDir newFolder()
    {
        await(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertTrue(instrument.invokeMenuActionSync(
                        activity, R.id.new_dir, 0));
            }
        });
        return new UiNewDir(instrument, activity);
    }

    public UiRename rename()
    {
        selectActionModeAction(R.id.rename);
        return new UiRename(instrument, activity);
    }

    public UiFileActivity copy()
    {
        selectActionModeAction(android.R.id.copy);
        waitForActionModeToFinish();
        return this;
    }

    public UiFileActivity paste()
    {
        await(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertTrue(instrument.invokeMenuActionSync(
                        activity, android.R.id.paste, 0));
            }
        });
        return this;
    }

    public UiFileActivity selectFromNavigationMode(final Resource dir)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final int position = activity.hierarchy().indexOf(dir);
                activity.onNavigationItemSelected(position, position);
            }
        });
        return this;
    }

    public UiFileActivity selectItem(final Resource resource)
    {
        return selectItem(new File(resource.uri()));
    }

    public UiFileActivity selectItem(final File file)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final ListView list = listView();
                final int position = findItemPositionOrThrow(file.getName());
                final int firstVisiblePosition = list.getFirstVisiblePosition();
                final int viewPosition = position - firstVisiblePosition;
                final View view = list.getChildAt(viewPosition);
                assertTrue(list.performItemClick(view, viewPosition, position));
            }
        });
        if (file.isDirectory())
        {
            assertCurrentDirectory(file);
        }
        return this;
    }

    public UiBookmarksFragment openBookmarksDrawer()
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                activity.getDrawerLayout().openDrawer(START);
            }
        });
        assertDrawerIsOpened(true);
        return new UiBookmarksFragment(instrument, (BookmarksFragment) activity
                .getFragmentManager().findFragmentById(R.id.bookmarks_fragment));
    }

    public UiFileActivity assertDrawerIsOpened(final boolean opened)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(
                        opened,
                        activity.getDrawerLayout().isDrawerOpen(START));
            }
        });
        return this;
    }

    public UiFileActivity pressBack()
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                activity.onBackPressed();
            }
        });
        return this;
    }

    public UiFileActivity longPressBack()
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertTrue(activity.onKeyLongPress(KeyEvent.KEYCODE_BACK, null));
            }
        });
        return this;
    }

    public UiFileActivity pressActionBarUpIndicator()
    {
        waitForUpIndicatorToAppear();
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final MenuItem item = mockMenuItem(android.R.id.home);
                assertTrue(activity.onOptionsItemSelected(item));
            }
        });
        return this;
    }

    public void waitForUpIndicatorToAppear()
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertTrue(
                        !activity.getActionBarDrawerToggle()
                                .isDrawerIndicatorEnabled());
            }
        });
    }

    public UiFileActivity assertCanRename(final boolean can)
    {
        assertEquals(can, renameMenu().isEnabled());
        return this;
    }

    public UiFileActivity assertCanPaste(final boolean can)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                activity.closeOptionsMenu();
                activity.openOptionsMenu();
            }
        });
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(can, pasteMenu().isEnabled());
            }
        });
        return this;
    }

    public UiFileActivity assertCurrentDirectory(final Resource expected)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final FilesFragment fragment = activity.fragment();
                final Resource actual = fragment.directory();
                assertEquals(expected, actual);
            }
        });
        return this;
    }

    public UiFileActivity assertCurrentDirectory(final File dir)
    {
        return assertCurrentDirectory(LocalResource.create(dir));
    }

    public UiFileActivity assertListViewContains(
            final Resource item,
            final boolean contains)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(
                        contains,
                        findItemPosition(item).isPresent());
            }
        });
        return this;
    }

    public UiFileActivity assertSummaryView(
            final Resource resource,
            final Consumer<CharSequence> assertion)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertion.apply(summaryView(resource).getText());
            }
        });
        return this;
    }

    public UiFileActivity assertActionBarTitle(final String title)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final int index = actionBar().getSelectedNavigationIndex();
                final Resource res = activity.hierarchy().get(index);
                assertEquals(title, label(res));
            }
        });
        return this;
    }

    private String label(final Resource res)
    {
        return FileLabels.get(activity.getResources(), res);
    }

    private ActionBar actionBar()
    {
        return activity.getActionBar();
    }

    public UiFileActivity assertActionBarUpIndicatorIsVisible(
            final boolean visible)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(
                        visible,
                        !activity.getActionBarDrawerToggle()
                                .isDrawerIndicatorEnabled());
            }
        });
        return this;
    }

    private TextView summaryView(final Resource resource)
    {
        return (TextView) view(resource).findViewById(R.id.summary);
    }

    private TextView iconView(final Resource resource)
    {
        return (TextView) view(resource).findViewById(R.id.icon);
    }

    private TextView titleView(final Resource resource)
    {
        return (TextView) view(resource).findViewById(R.id.title);
    }

    private View view(final Resource resource)
    {
        final View view = view(resource.name().toString());
        assertNotNull(view);
        return view;
    }

    private View view(final String name)
    {
        final ListView list = listView();
        final int index = findItemPositionOrThrow(name);
        return list.getChildAt(index - list.getFirstVisiblePosition());
    }

    private ListView listView()
    {
        return (ListView) activity
                .fragment()
                .getView()
                .findViewById(android.R.id.list);
    }

    private MenuItem renameMenu()
    {
        return activity.getCurrentActionMode().getMenu().findItem(R.id.rename);
    }

    private MenuItem pasteMenu()
    {
        return activity.getMenu().findItem(android.R.id.paste);
    }

    void selectActionModeAction(final int id)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final ActionMode mode = activity.getCurrentActionMode();
                final MenuItem item = mode.getMenu().findItem(id);
                assertTrue(activity
                        .getCurrentActionModeCallback()
                        .onActionItemClicked(mode, item));
            }
        });
    }

    void waitForActionModeToFinish()
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertNull(activity.getCurrentActionMode());
            }
        });
    }

    private int findItemPositionOrThrow(final Resource resource)
    {
        return findItemPositionOrThrow(resource.name().toString());
    }

    private int findItemPositionOrThrow(final String filename)
    {
        final Optional<Integer> position = findItemPosition(filename);
        if (position.isPresent())
        {
            return position.get();
        }
        throw new AssertionError("No file with name: " + filename);
    }

    private Optional<Integer> findItemPosition(final Resource resource)
    {
        return findItemPosition(resource.name().toString());
    }

    private Optional<Integer> findItemPosition(final String filename)
    {
        final int count = listView().getCount();
        for (int i = 0; i < count; i++)
        {
            final FileListItem item = (FileListItem)
                    listView().getItemAtPosition(i);

            if (item.isFile() &&
                    ((FileListItem.File) item).resource().name().toString()
                            .equals(filename))
            {
                return Optional.of(i);
            }
        }
        return Optional.absent();
    }

    /**
     * Clicks the "Select All" action item.
     */
    public UiFileActivity selectAll()
    {
        selectActionModeAction(android.R.id.selectAll);
        return this;
    }

    /**
     * Asserts whether the given item is currently checked.
     */
    public UiFileActivity assertChecked(
            final File file, final boolean checked)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final int position = findItemPositionOrThrow(file.getName());
                assertEquals(checked, listView().isItemChecked(position));
            }
        });
        return this;
    }

    /**
     * Asserts whether the activity currently in an action mode.
     */
    public UiFileActivity assertActionModePresent(final boolean present)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(present, activity.getCurrentActionMode() != null);
            }
        });
        return this;
    }

    public UiFileActivity assertBookmarkMenuChecked(final boolean checked)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final MenuItem item = activity.getMenu().findItem(R.id.bookmark);
                assertEquals(checked, item.isChecked());
            }
        });
        return this;
    }

    public UiFileActivity assertSymbolicLinkIconDisplayed(
            final Resource resource,
            final boolean displayed)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final View view = view(resource).findViewById(R.id.symlink);
                if (displayed)
                {
                    assertEquals(VISIBLE, view.getVisibility());
                }
                else
                {
                    assertEquals(GONE, view.getVisibility());
                }
            }
        });
        return this;
    }

    public UiFileActivity assertBookmarksSidebarIsOpenLocked(
            final boolean openLocked)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertEquals(
                        openLocked,
                        LOCK_MODE_LOCKED_OPEN == activity.getDrawerLayout()
                                .getDrawerLockMode(START));
            }
        });
        return this;
    }

    public UiFileActivity assertDisabled(final Resource resource)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertFalse(iconView(resource).isEnabled());
                assertFalse(titleView(resource).isEnabled());
                assertFalse(summaryView(resource).isEnabled());
            }
        });
        return this;
    }

    public UiFileActivity assertNavigationModeHierarchy(final Resource dir)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final List<Resource> expected = dir.hierarchy().reverse();
                final List<Resource> actual = activity.hierarchy();
                assertEquals(expected, actual);
                assertEquals(
                        expected.indexOf(dir),
                        actionBar().getSelectedNavigationIndex());
            }
        });
        return this;
    }

}
