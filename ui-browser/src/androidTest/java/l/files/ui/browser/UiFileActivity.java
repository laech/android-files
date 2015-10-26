package l.files.ui.browser;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import l.files.fs.File;
import l.files.fs.Stat;
import l.files.fs.Stream;
import l.files.ui.R;
import l.files.ui.base.fs.FileLabels;
import l.files.ui.base.view.Views;
import l.files.ui.browser.BrowserItem.FileItem;

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
import static java.util.concurrent.TimeUnit.MINUTES;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.R.drawable.ic_arrow_back_white_24dp;
import static l.files.ui.R.drawable.ic_menu_white_24dp;
import static l.files.ui.base.view.Views.find;
import static l.files.ui.browser.Instrumentations.await;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;
import static l.files.ui.browser.Instrumentations.clickItemOnMainThread;
import static l.files.ui.browser.Instrumentations.longClickItemOnMainThread;
import static l.files.ui.browser.Mocks.mockMenuItem;

final class UiFileActivity {

    private final Instrumentation instrument;
    private FilesActivity activity;

    UiFileActivity(
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

    UiFileActivity refresh() {
        selectMenuAction(R.id.refresh);
        return this;
    }

    UiFileActivity bookmark() {
        assertBookmarkMenuChecked(false);
        selectMenuAction(R.id.bookmark);
        return this;
    }

    UiFileActivity unbookmark() {
        assertBookmarkMenuChecked(true);
        selectMenuAction(R.id.bookmark);
        return this;
    }

    UiNewDir newFolder() {
        selectMenuAction(R.id.new_dir);
        return new UiNewDir(this);
    }

    UiRename rename() {
        selectActionModeAction(R.id.rename);
        return new UiRename(this);
    }

    UiDelete delete() {
        selectActionModeAction(R.id.delete);
        return new UiDelete(this);
    }

    UiFileActivity copy() {
        selectActionModeAction(android.R.id.copy);
        waitForActionModeToFinish();
        return this;
    }

    UiFileActivity cut() {
        selectActionModeAction(android.R.id.cut);
        waitForActionModeToFinish();
        return this;
    }

    UiFileActivity paste() {
        selectMenuAction(android.R.id.paste);
        return this;
    }

    UiSort sort() {
        selectMenuAction(R.id.sort_by);
        return new UiSort(this);
    }

    UiFileActivity selectFromNavigationMode(final File dir) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                int position = activity().hierarchy().indexOf(dir);
                activity().title().setSelection(position);
            }
        });
        return this;
    }

    UiFileActivity clickInto(File file) {
        click(file);
        assertCurrentDirectory(file);
        return this;
    }

    UiFileActivity click(File file) {
        clickItemOnMainThread(instrument, recycler(), file);
        return this;
    }

    UiFileActivity longClick(File file) {
        longClickItemOnMainThread(instrument, recycler(), file);
        return this;
    }

    UiBookmarksFragment openBookmarksDrawer() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                activity().drawerLayout().openDrawer(START);
            }
        });
        return new UiBookmarksFragment(this);
    }

    UiFileActivity assertDrawerIsOpened(final boolean opened) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(opened, activity().drawerLayout().isDrawerOpen(START));
            }
        });
        return this;
    }

    UiFileActivity pressBack() {
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

    UiFileActivity longPressBack() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertTrue(activity().onKeyLongPress(KEYCODE_BACK, null));
            }
        });
        return this;
    }

    UiFileActivity pressActionBarUpIndicator() {
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
                        activity().getResources().getDrawable(ic_arrow_back_white_24dp),
                        activity().toolbar().getNavigationIcon());
            }
        });
    }

    UiFileActivity assertCanRename(boolean can) {
        assertEquals(can, renameMenu().isEnabled());
        return this;
    }

    UiFileActivity assertCanPaste(final boolean can) {
        return findOptionMenuItem(android.R.id.paste, new Consumer<MenuItem>() {
            @Override
            public void apply(MenuItem input) {
                assertEquals(can, input.isEnabled());
            }
        });
    }

    private UiFileActivity findOptionMenuItem(
            final int id, final Consumer<MenuItem> consumer) {

        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                Toolbar toolbar = activity().toolbar();
                toolbar.hideOverflowMenu();
                toolbar.showOverflowMenu();
                MenuItem item = toolbar.getMenu().findItem(id);
                assertNotNull(item);
                consumer.apply(item);
                toolbar.hideOverflowMenu();
            }
        });
        return this;
    }

    private UiFileActivity clickOptionMenuItem(final int id) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                activity().toolbar().getMenu().performIdentifierAction(id, 0);
            }
        });
        return this;
    }

    UiFileActivity assertCurrentDirectory(final File expected) {
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

    UiFileActivity assertListViewContains(
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

    UiFileActivity assertSummaryView(
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

    UiFileActivity assertActionBarTitle(final String title) {
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

    UiFileActivity assertActionBarUpIndicatorIsVisible(
            final boolean visible) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                if (visible) {
                    assertBitmapEquals(
                            activity().getResources().getDrawable(ic_arrow_back_white_24dp),
                            activity().toolbar().getNavigationIcon());
                } else {
                    assertBitmapEquals(
                            activity().getResources().getDrawable(ic_menu_white_24dp),
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
        //noinspection ConstantConditions
        return activity().currentActionMode().getMenu().findItem(R.id.rename);
    }

    private UiFileActivity selectMenuAction(int id) {
        findOptionMenuItem(id, new Consumer<MenuItem>() {
            @Override
            public void apply(MenuItem item) {
                assertTrue(item.isEnabled());
            }
        });
        return clickOptionMenuItem(id);
    }

    void selectActionModeAction(final int id) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                ActionMode mode = activity().currentActionMode();
                assertNotNull(mode);
                MenuItem item = mode.getMenu().findItem(id);
                //noinspection ConstantConditions
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
    UiFileActivity selectAll() {
        selectActionModeAction(android.R.id.selectAll);
        return this;
    }

    /**
     * Asserts whether the given item is currently checked.
     */
    UiFileActivity assertChecked(
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
    UiFileActivity assertActionModePresent(final boolean present) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(present, activity().currentActionMode() != null);
            }
        });
        return this;
    }

    UiFileActivity assertActionModeTitle(final Object title) {
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

    UiFileActivity assertBookmarkMenuChecked(final boolean checked) {
        return findOptionMenuItem(R.id.bookmark, new Consumer<MenuItem>() {
            @Override
            public void apply(MenuItem item) {
                assertEquals(checked, item.isChecked());
            }
        });
    }

    UiFileActivity assertRefreshMenuVisible(final boolean visible) {
        return findOptionMenuItem(R.id.refresh, new Consumer<MenuItem>() {
            @Override
            public void apply(MenuItem input) {
                assertEquals(visible, input.isVisible());
            }
        });
    }

    UiFileActivity assertThumbnailShown(
            File file, final boolean shown) {

        findItemOnMainThread(file, new Consumer<View>() {
            @Override
            public void apply(View input) {
                ImageView view = Views.find(R.id.preview, input);
                if (shown) {
                    assertEquals(VISIBLE, view.getVisibility());
                    assertNotNull(view.getDrawable());
                } else {
                    assertEquals(GONE, view.getVisibility());
                    assertNull(view.getDrawable());
                }
            }
        });
        return this;
    }

    UiFileActivity assertLinkIconDisplayed(
            File file, final boolean displayed) {

        findItemOnMainThread(file, new Consumer<View>() {
            @Override
            public void apply(View input) {
                View view = input.findViewById(R.id.link_icon);
                if (displayed) {
                    assertEquals(VISIBLE, view.getVisibility());
                } else {
                    assertEquals(GONE, view.getVisibility());
                }
            }
        });
        return this;
    }

    UiFileActivity assertLinkPathDisplayed(
            File link, final File target) {

        findItemOnMainThread(link, new Consumer<View>() {
            @Override
            public void apply(View input) {

                TextView view = Views.find(R.id.link_path, input);
                if (target != null) {

                    String actual = view.getText().toString();
                    assertTrue(
                            actual + " to contain " + target.path(),
                            actual.contains(target.path()));
                    assertEquals(VISIBLE, view.getVisibility());

                } else {
                    assertEquals(GONE, view.getVisibility());
                }

            }
        });
        return this;
    }

    UiFileActivity assertBookmarksSidebarIsOpenLocked(
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

    UiFileActivity assertBookmarksSidebarIsClosed() {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(false, activity().drawerLayout().isDrawerOpen(START));
            }
        });
        return this;
    }

    UiFileActivity assertDisabled(File file) {
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

    UiFileActivity assertNavigationModeHierarchy(final File dir) {
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

    UiFileActivity assertListMatchesFileSystem(final File dir)
            throws IOException {

        await(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Pair<File, Stat> fileNotInView = null;
                Pair<File, Stat> fileNotInFs = null;

                Set<Pair<File, Stat>> filesInView = filesInView();
                try (Stream<File> stream = dir.list(FOLLOW)) {
                    for (File child : stream) {
                        Pair<File, Stat> item = Pair.create(child, child.stat(NOFOLLOW));
                        if (!filesInView.remove(item)) {
                            fileNotInView = item;
                        }
                    }
                }

                if (!filesInView.isEmpty()) {
                    fileNotInFs = filesInView.iterator().next();
                }

                if (fileNotInView != null || fileNotInFs != null) {
                    fail("Details do not match."
                                    + "\nin view: " + toString(fileNotInFs)
                                    + "\nin fs:   " + toString(fileNotInView)
                    );
                }

                return null;

            }

            private String toString(Pair<File, ?> pair) {
                return pair == null ? null : (pair.first.name()) + "=" + pair.second;
            }

        });

        return this;
    }

    private Set<Pair<File, Stat>> filesInView() {
        List<FileItem> items = fileItems();
        Set<Pair<File, Stat>> result = new HashSet<>(items.size() * 2);
        for (FileItem item : items) {
            result.add(Pair.create(item.selfFile(), item.selfStat()));
        }
        return result;
    }

    private List<FileItem> fileItems() {
        List<BrowserItem> items = fragment().items();
        List<FileItem> files = new ArrayList<>(items.size());
        for (BrowserItem item : items) {
            if (item.isFileItem()) {
                files.add(((FileItem) item));
            }
        }
        return files;
    }

    private List<File> resources() {
        List<FileItem> items = fileItems();
        List<File> files = new ArrayList<>(items.size());
        for (FileItem item : items) {
            files.add(item.selfFile());
        }
        return files;
    }

    UiFileActivity assertItemsDisplayed(final File... expected) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                List<File> actual = new ArrayList<>();
                for (FileItem item : fileItems()) {
                    actual.add(item.selfFile());
                }
                assertEquals(asList(expected), actual);
            }
        });
        return this;
    }

    UiFileActivity rotate() {
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
