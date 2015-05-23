package l.files.features.objects;

import android.app.Instrumentation;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Optional;

import java.io.File;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.Resource;
import l.files.fs.local.LocalResource;
import l.files.ui.FilesActivity;
import l.files.ui.bookmarks.BookmarksFragment;
import l.files.ui.browser.FileListItem;
import l.files.ui.browser.FilesFragment;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.view.Gravity.START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
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
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                getListView().setItemChecked(
                        findItemPositionOrThrow(file.getName()), checked);
            }
        });
        return this;
    }

    public UiNewFolder newFolder()
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
        return new UiNewFolder(instrument, activity);
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
                final ListView list = getListView();
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
        assertEquals(can, getRenameMenuItem().isEnabled());
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
                assertEquals(can, getPasteMenuItem().isEnabled());
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
                        findItemPosition(item.name()).isPresent());
            }
        });
        return this;
    }

    public UiFileActivity assertFileModifiedDateView(
            final Resource resource,
            final Consumer<CharSequence> assertion)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                assertion.apply(getFileModifiedView(resource).getText());
            }
        });
        return this;
    }

    public UiFileActivity assertFileSizeView(
            final Resource resource,
            final Consumer<CharSequence> assertion)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final CharSequence actual = getFileSizeView(resource).getText();
                assertion.apply(actual);
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
                //noinspection ConstantConditions
                assertEquals(title, activity.getActionBar().getTitle());
            }
        });
        return this;
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

    private TextView getFileModifiedView(final Resource resource)
    {
        return (TextView) getView(resource).findViewById(R.id.date);
    }

    private TextView getFileSizeView(final Resource resource)
    {
        return (TextView) getView(resource).findViewById(R.id.size);
    }

    private TextView getFileIconView(final Resource resource)
    {
        return (TextView) getView(resource).findViewById(R.id.icon);
    }

    private TextView getFileTitleView(final Resource resource)
    {
        return (TextView) getView(resource).findViewById(R.id.title);
    }

    private View getView(final Resource resource)
    {
        return getView(resource.name());
    }

    private View getView(final String name)
    {
        final ListView list = getListView();
        final int index = findItemPositionOrThrow(name);
        return list.getChildAt(index - list.getFirstVisiblePosition());
    }

    private ListView getListView()
    {
        return (ListView) activity
                .fragment()
                .getView()
                .findViewById(android.R.id.list);
    }

    private MenuItem getRenameMenuItem()
    {
        return activity.getCurrentActionMode().getMenu().findItem(R.id.rename);
    }

    private MenuItem getPasteMenuItem()
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

    private int findItemPositionOrThrow(final String filename)
    {
        final Optional<Integer> position = findItemPosition(filename);
        if (position.isPresent())
        {
            return position.get();
        }
        throw new AssertionError("No file with name: " + filename);
    }

    private Optional<Integer> findItemPosition(final String filename)
    {
        final int count = getListView().getCount();
        for (int i = 0; i < count; i++)
        {
            final FileListItem item = (FileListItem)
                    getListView().getItemAtPosition(i);

            if (item.isFile() &&
                    ((FileListItem.File) item).getResource().name()
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
                assertEquals(checked, getListView().isItemChecked(position));
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
                final View view = getView(resource).findViewById(R.id.symlink);
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
                assertFalse(getFileIconView(resource).isEnabled());
                assertFalse(getFileSizeView(resource).isEnabled());
                assertFalse(getFileTitleView(resource).isEnabled());
                assertFalse(getFileModifiedView(resource).isEnabled());
            }
        });
        return this;
    }
}
