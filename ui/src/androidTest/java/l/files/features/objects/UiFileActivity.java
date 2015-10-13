package l.files.features.objects;

import android.app.Fragment;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import l.files.common.base.Consumer;
import l.files.common.base.Provider;
import l.files.fs.File;
import l.files.fs.Stat;
import l.files.fs.Stream;
import l.files.ui.FileLabels;
import l.files.ui.R;
import l.files.ui.browser.FileListItem;
import l.files.ui.browser.FilesActivity;
import l.files.ui.browser.FilesFragment;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.support.v4.view.GravityCompat.START;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Objects.requireNonNull;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static l.files.common.view.Views.find;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;
import static l.files.features.objects.Instrumentations.clickItemOnMainThread;
import static l.files.features.objects.Instrumentations.longClickItemOnMainThread;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.testing.Mocks.mockMenuItem;

public final class UiFileActivity {

    private final Instrumentation instrument;
    private FilesActivity activity;

    public UiFileActivity(
            final Instrumentation instrumentation,
            final Provider<FilesActivity> provider) {

        requireNonNull(instrumentation);
        requireNonNull(provider);

        this.instrument = instrumentation;
        this.activity = provider.get();
    }

    private FilesFragment fragment() {
        Fragment fragment = activity().fragment();
        assertNotNull(fragment);
        return (FilesFragment) fragment;
    }

    FilesActivity activity() {
        return activity;
    }

    Instrumentation instrumentation() {
        return instrument;
    }

    public UiFileActivity bookmark() {
        assertBookmarkMenuChecked(false);
        selectMenuAction(R.id.bookmark);
        return this;
    }

    public UiFileActivity unbookmark() {
        assertBookmarkMenuChecked(true);
        selectMenuAction(R.id.bookmark);
        return this;
    }

    public UiNewDir newFolder() {
        selectMenuAction(R.id.new_dir);
        return new UiNewDir(this);
    }

    public UiRename rename() {
        selectActionModeAction(R.id.rename);
        return new UiRename(this);
    }

    public UiFileActivity copy() {
        selectActionModeAction(android.R.id.copy);
        waitForActionModeToFinish();
        return this;
    }

    public UiFileActivity paste() {
        selectMenuAction(android.R.id.paste);
        return this;
    }

    public UiSort sort() {
        selectMenuAction(R.id.sort_by);
        return new UiSort(this);
    }

    public UiFileActivity selectFromNavigationMode(final File dir) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                int position = activity().hierarchy().indexOf(dir);
                activity().title().setSelection(position);
            }
        });
        return this;
    }

    public UiFileActivity clickInto(File file) {
        click(file);
        assertCurrentDirectory(file);
        return this;
    }

    public UiFileActivity click(File file) {
        clickItemOnMainThread(instrument, recycler(), file);
        return this;
    }

    public UiFileActivity longClick(File file) {
        longClickItemOnMainThread(instrument, recycler(), file);
        return this;
    }

    public UiBookmarksFragment openBookmarksDrawer() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                activity().drawerLayout().openDrawer(START);
            }
        });
        assertDrawerIsOpened(true);
        return new UiBookmarksFragment(this);
    }

    public UiFileActivity assertDrawerIsOpened(final boolean opened) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(opened, activity().drawerLayout().isDrawerOpen(START));
            }
        });
        return this;
    }

    public UiFileActivity pressBack() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                // This is to wait for existing messages on the main thread
                // queue to be cleared first
            }
        });
        instrument.sendKeyDownUpSync(KEYCODE_BACK);
        return this;
    }

    public UiFileActivity longPressBack() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertTrue(activity().onKeyLongPress(KEYCODE_BACK, null));
            }
        });
        return this;
    }

    public UiFileActivity pressActionBarUpIndicator() {
        waitForUpIndicatorToAppear();
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                MenuItem item = mockMenuItem(android.R.id.home);
                assertTrue(activity().onOptionsItemSelected(item));
            }
        });
        return this;
    }

    public void waitForUpIndicatorToAppear() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertBitmapEquals(
                        activity().getDrawable(R.drawable.ic_arrow_back_white_24dp),
                        activity().toolbar().getNavigationIcon());
            }
        });
    }

    public UiFileActivity assertCanRename(boolean can) {
        assertEquals(can, renameMenu().isEnabled());
        return this;
    }

    public UiFileActivity assertCanPaste(final boolean can) {
        return findOptionMenuItem(android.R.id.paste, new Consumer<MenuItem>() {
            @Override
            public void apply(MenuItem input) {
                assertEquals(can, input.isEnabled());
            }
        });
    }

    private UiFileActivity findOptionMenuItem(
            final int id,
            final Consumer<MenuItem> consumer) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                activity().toolbar().hideOverflowMenu();
                activity().toolbar().showOverflowMenu();
            }
        });
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                MenuItem item = activity().toolbar().getMenu().findItem(id);
                assertNotNull(item);
                consumer.apply(item);
            }
        });
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                activity().toolbar().hideOverflowMenu();
            }
        });
        return this;
    }

    public UiFileActivity assertCurrentDirectory(final File expected) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                FilesFragment fragment = activity().fragment();
                assertNotNull(fragment);
                File actual = fragment.directory();
                assertEquals(expected, actual);
            }
        });
        return this;
    }

    public UiFileActivity assertListViewContains(
            final File item,
            final boolean contains) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(contains, resources().contains(item));
            }
        });
        return this;
    }

    public UiFileActivity assertSummaryView(
            final File file,
            final Consumer<CharSequence> assertion) {
        findItemOnMainThread(file, new Consumer<View>() {
            @Override
            public void apply(View input) {
                TextView summary = find(R.id.summary, input);
                assertion.apply(summary.getText());
            }
        });
        return this;
    }

    public UiFileActivity assertActionBarTitle(final String title) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(title, label((File) activity().title().getSelectedItem()));
            }
        });
        return this;
    }

    private String label(File file) {
        return FileLabels.get(activity().getResources(), file);
    }

    public UiFileActivity assertActionBarUpIndicatorIsVisible(
            final boolean visible) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                if (visible) {
                    assertBitmapEquals(
                            activity().getDrawable(R.drawable.ic_arrow_back_white_24dp),
                            activity().toolbar().getNavigationIcon());
                } else {
                    assertBitmapEquals(
                            activity().getDrawable(R.drawable.ic_menu_white_24dp),
                            activity().toolbar().getNavigationIcon());
                }
            }
        });
        return this;
    }

    private static void assertBitmapEquals(Drawable expected, Drawable actual) {
        Bitmap bitmapExpected = ((BitmapDrawable) expected).getBitmap();
        Bitmap bitmapActual = ((BitmapDrawable) actual).getBitmap();
        assertEquals(bitmapExpected.getWidth(), bitmapActual.getWidth());
        assertEquals(bitmapExpected.getHeight(), bitmapActual.getHeight());
        for (int x = 0; x < bitmapExpected.getWidth(); x++) {
            for (int y = 0; y < bitmapExpected.getHeight(); y++) {
                assertEquals(
                        bitmapExpected.getPixel(x, y),
                        bitmapActual.getPixel(x, y));
            }
        }
    }

    private void findItemOnMainThread(
            File file,
            Consumer<View> consumer) {
        Instrumentations.findItemOnMainThread(
                instrument, recycler(), file, consumer);
    }

    private Provider<RecyclerView> recycler() {
        return new Provider<RecyclerView>() {
            @Override
            public RecyclerView get() {

                return awaitOnMainThread(instrument, new Callable<RecyclerView>() {
                    @Override
                    public RecyclerView call() throws Exception {
                        return fragment().recycler;
                    }
                });

            }
        };
    }

    private MenuItem renameMenu() {
        return activity().currentActionMode().getMenu().findItem(R.id.rename);
    }

    private UiFileActivity selectMenuAction(int id) {
        findOptionMenuItem(id, new Consumer<MenuItem>() {
            @Override
            public void apply(MenuItem item) {
                assertTrue(item.isEnabled());
            }
        });
        instrument.invokeMenuActionSync(activity(), id, 0);
        return this;
    }

    void selectActionModeAction(final int id) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                ActionMode mode = activity().currentActionMode();
                MenuItem item = mode.getMenu().findItem(id);
                assertTrue(activity()
                        .currentActionModeCallback()
                        .onActionItemClicked(mode, item));
            }
        });
    }

    void waitForActionModeToFinish() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertNull(activity().currentActionMode());
            }
        });
    }

    /**
     * Clicks the "Select All" action item.
     */
    public UiFileActivity selectAll() {
        selectActionModeAction(android.R.id.selectAll);
        return this;
    }

    /**
     * Asserts whether the given item is currently checked.
     */
    public UiFileActivity assertChecked(
            File file, final boolean checked) {
        findItemOnMainThread(file, new Consumer<View>() {
            @Override
            public void apply(View view) {
                assertEquals(checked, view.isActivated());
            }
        });
        return this;
    }

    /**
     * Asserts whether the activity.get() currently in an action mode.
     */
    public UiFileActivity assertActionModePresent(final boolean present) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(present, activity().currentActionMode() != null);
            }
        });
        return this;
    }

    public UiFileActivity assertActionModeTitle(final Object title) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                ActionMode mode = activity().currentActionMode();
                assertNotNull(mode);
                assertEquals(title.toString(), mode.getTitle().toString());
            }
        });
        return this;
    }

    public UiFileActivity assertBookmarkMenuChecked(final boolean checked) {
        return findOptionMenuItem(R.id.bookmark, new Consumer<MenuItem>() {
            @Override
            public void apply(MenuItem item) {
                assertEquals(checked, item.isChecked());
            }
        });
    }

    public UiFileActivity assertSymbolicLinkIconDisplayed(
            File file,
            final boolean displayed) {
        findItemOnMainThread(file, new Consumer<View>() {
            @Override
            public void apply(View input) {
                View view = input.findViewById(R.id.symlink);
                if (displayed) {
                    assertEquals(VISIBLE, view.getVisibility());
                } else {
                    assertEquals(GONE, view.getVisibility());
                }
            }
        });
        return this;
    }

    public UiFileActivity assertBookmarksSidebarIsOpenLocked(
            final boolean openLocked) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(
                        openLocked,
                        LOCK_MODE_LOCKED_OPEN == activity().drawerLayout()
                                .getDrawerLockMode(START));
            }
        });
        return this;
    }

    public UiFileActivity assertBookmarksSidebarIsClosed() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(false, activity().drawerLayout().isDrawerOpen(START));
            }
        });
        return this;
    }

    public UiFileActivity assertDisabled(File file) {
        findItemOnMainThread(file, new Consumer<View>() {
            @Override
            public void apply(View input) {
                assertFalse(input.findViewById(R.id.icon).isEnabled());
                assertFalse(input.findViewById(R.id.title).isEnabled());
                assertFalse(input.findViewById(R.id.summary).isEnabled());
            }
        });
        return this;
    }

    public UiFileActivity assertNavigationModeHierarchy(final File dir) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                List<File> actual = activity().hierarchy();
                List<File> expected = new ArrayList<>(dir.hierarchy());
                reverse(expected);
                assertEquals(expected, actual);
                assertEquals(dir, activity().title().getSelectedItem());
            }
        });
        return this;
    }

    public UiFileActivity assertListViewContainsChildrenOf(
            final File dir) throws IOException {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(
                        childrenStatsSortedByPath(dir),
                        listViewStatsSortedByPath());
            }
        });
        return this;
    }

    private List<Pair<File, Stat>> childrenStatsSortedByPath(File dir) {
        try (Stream<File> stream = dir.list(FOLLOW)) {
            List<File> children = sortResourcesByPath(stream);
            return stat(children);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Pair<File, Stat>> stat(
            List<File> files) throws IOException {
        List<Pair<File, Stat>> result = new ArrayList<>();
        for (File file : files) {
            result.add(Pair.create(file, file.stat(NOFOLLOW)));
        }
        return result;
    }

    private List<File> sortResourcesByPath(Stream<File> iterable) {
        List<File> files = iterable.to(new ArrayList<File>());
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.path().compareTo(b.path());
            }
        });
        return files;
    }

    private List<Pair<File, Stat>> listViewStatsSortedByPath() {
        List<FileListItem.File> items = sortFilesByPath(fileItems());
        return stats(items);
    }

    private List<Pair<File, Stat>> stats(List<FileListItem.File> items) {
        List<Pair<File, Stat>> result = new ArrayList<>();
        for (FileListItem.File item : items) {
            result.add(Pair.create(item.file(), item.stat()));
        }
        return result;
    }

    private List<FileListItem.File> sortFilesByPath(
            List<FileListItem.File> items) {
        Collections.sort(items, new Comparator<FileListItem.File>() {
            @Override
            public int compare(
                    FileListItem.File a,
                    FileListItem.File b) {
                return a.file().path().compareTo(b.file().path());
            }
        });
        return items;
    }

    private List<FileListItem.File> fileItems() {
        List<FileListItem> items = fragment().items();
        List<FileListItem.File> files = new ArrayList<>(items.size());
        for (FileListItem item : items) {
            if (item.isFile()) {
                files.add(((FileListItem.File) item));
            }
        }
        return files;
    }

    private List<File> resources() {
        List<FileListItem.File> items = fileItems();
        List<File> files = new ArrayList<>(items.size());
        for (FileListItem.File item : items) {
            files.add(item.file());
        }
        return files;
    }

    public UiFileActivity assertItemsDisplayed(final File... expected) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                List<File> actual = new ArrayList<>();
                for (FileListItem.File item : fileItems()) {
                    actual.add(item.file());
                }
                assertEquals(asList(expected), actual);
            }
        });
        return this;
    }

    public UiFileActivity rotate() {
        ActivityMonitor monitor = new ActivityMonitor(
                FilesActivity.class.getName(), null, false);

        instrument.addMonitor(monitor);

        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                activity().setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
                activity().setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
                activity().setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
            }
        });

        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                // This is waiting for previous action to be cleared in the UI thread
            }
        });

        activity = (FilesActivity) monitor.getLastActivity();
        assertNotNull(activity);
        instrument.removeMonitor(monitor);
        return this;
    }
}
