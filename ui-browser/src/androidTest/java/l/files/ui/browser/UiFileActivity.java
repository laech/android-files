package l.files.ui.browser;

import android.app.Instrumentation;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import l.files.base.Provider;
import l.files.fs.FileSystem;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.FileLabels;
import l.files.ui.base.view.Views;

import static android.support.v4.view.GravityCompat.START;
import static android.view.KeyEvent.KEYCODE_BACK;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.concurrent.TimeUnit.MINUTES;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.Files.stat;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.Instrumentations.await;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;
import static l.files.ui.browser.Instrumentations.clickItemOnMainThread;
import static l.files.ui.browser.Instrumentations.longClickItemOnMainThread;
import static l.files.ui.browser.Mocks.mockMenuItem;

final class UiFileActivity {

    private final Instrumentation instrument;
    private Provider<FilesActivity> activity;

    UiFileActivity(
            final Instrumentation instrumentation,
            final Provider<FilesActivity> provider) {

        requireNonNull(instrumentation);
        requireNonNull(provider);

        this.instrument = instrumentation;
        this.activity = provider;
    }

    private FilesFragment fragment() {
        Fragment fragment = activity().fragment();
        assertNotNull(fragment);
        return (FilesFragment) fragment;
    }

    FilesActivity activity() {
        return activity.get();
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

    UiFileActivity selectFromNavigationMode(final Path dir) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                int position = activity().hierarchy().indexOf(dir);
                activity().title().setSelection(position);
            }
        });
        return this;
    }

    UiFileActivity clickInto(Path file) {
        click(file);
        assertCurrentDirectory(file);
        return this;
    }

    UiFileActivity click(Path file) {
        clickItemOnMainThread(instrument, recycler(), file);
        return this;
    }

    UiFileActivity longClick(Path file) {
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
        assertDrawerIsOpened(true);
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
                assertEquals(1F, activity().navigationIcon().getProgress());
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
                String msg = "Paste menu enabled to be " + can;
                assertEquals(msg, can, input.isEnabled());
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

    UiFileActivity assertCurrentDirectory(final Path expected) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                FilesFragment fragment = activity().fragment();
                assertNotNull(fragment);
                Path actual = fragment.directory();
                assertEquals(expected, actual);
            }
        });
        return this;
    }

    UiFileActivity assertListViewContains(
            final Path item,
            final boolean contains) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(contains, resources().contains(item));
            }
        });
        return this;
    }

    UiFileActivity assertItemContentView(
            final Path file,
            final Consumer<FileView> assertion) {
        findItemOnMainThread(file, new Consumer<View>() {
            @Override
            public void apply(View input) {
                assertion.apply(Views.<FileView>find(android.R.id.content, input));
            }
        });
        return this;
    }

    UiFileActivity assertActionBarTitle(final String title) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(title, label((Path) activity().title().getSelectedItem()));
            }
        });
        return this;
    }

    private String label(Path file) {
        return FileLabels.get(activity().getResources(), file);
    }

    UiFileActivity assertActionBarUpIndicatorIsVisible(final boolean visible) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                assertEquals(visible ? 1F : 0F, activity().navigationIcon().getProgress());
            }
        });
        return this;
    }

    private void findItemOnMainThread(
            Path file,
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
            Path file, final boolean checked) {
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
                String msg = "Refresh menu visible to be " + visible;
                assertEquals(msg, visible, input.isVisible());
            }
        });
    }

    UiFileActivity assertThumbnailShown(Path file, final boolean shown) {

        assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                assertEquals(shown, input.hasPreviewContent());
            }
        });

        return this;
    }

    UiFileActivity assertLinkIconDisplayed(Path file, final boolean displayed) {

        assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                assertEquals(displayed, input.isLinkIconVisible());
            }
        });

        return this;
    }

    UiFileActivity assertLinkPathDisplayed(Path link, final Path target) {

        assertItemContentView(link, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                if (target != null) {
                    CharSequence expected = input.getResources().getString(
                            R.string.link_x, target);
                    CharSequence actual = input.getLink().getText();
                    assertEquals(expected, actual);
                }
            }
        });

        return this;
    }

    UiFileActivity assertSummary(Path file, final CharSequence expected) {

        assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView view) {
                assertEquals(expected, view.getSummary().getText());
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

    UiFileActivity assertDisabled(Path file) {
        assertItemContentView(file, new Consumer<FileView>() {
            @Override
            public void apply(FileView input) {
                assertFalse(input.isEnabled());
            }
        });
        return this;
    }

    UiFileActivity assertNavigationModeHierarchy(final Path dir) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                List<Path> actual = activity().hierarchy();
                List<Path> expected = new ArrayList<>(Files.hierarchy(dir));
                reverse(expected);
                assertEquals(expected, actual);
                assertEquals(dir, activity().title().getSelectedItem());
            }
        });
        return this;
    }

    UiFileActivity assertListMatchesFileSystem(Path dir)
            throws IOException {
        return assertListMatchesFileSystem(dir, 1, MINUTES);
    }

    UiFileActivity assertListMatchesFileSystem(
            final Path dir,
            final int timeout,
            final TimeUnit timeoutUnit)
            throws IOException {

        await(new Callable<Void>() {
            @Override
            public Void call() throws Exception {


                final SimpleArrayMap<Path, Stat> filesInView = filesInView();

                Files.list(dir, FOLLOW, new FileSystem.Consumer<Path>() {
                    @Override
                    public boolean accept(Path child) throws IOException {
                        Stat oldStat = filesInView.remove(child);
                        if (oldStat == null) {
                            fail("Path in file system but not in view: " + child);
                        }

                        Stat newStat = stat(child, NOFOLLOW);
                        if (!newStat.equals(oldStat)) {
                            fail("Path details differ for : " + child
                                    + "\nnew: " + newStat
                                    + "\nold: " + oldStat);
                        }
                        return true;
                    }
                });

                if (!filesInView.isEmpty()) {
                    fail("Path in view but not on file system: "
                            + filesInView.keyAt(0) + "="
                            + filesInView.valueAt(0));
                }

                return null;

            }

        }, timeout, timeoutUnit);

        return this;
    }

    private SimpleArrayMap<Path, Stat> filesInView() {
        List<FileInfo> items = fileItems();
        SimpleArrayMap<Path, Stat> result = new SimpleArrayMap<>(items.size());
        for (FileInfo item : items) {
            result.put(item.selfPath(), item.selfStat());
        }
        return result;
    }

    private List<FileInfo> fileItems() {
        List<Object> items = fragment().items();
        List<FileInfo> files = new ArrayList<>(items.size());
        for (Object item : items) {
            if (item instanceof FileInfo) {
                files.add(((FileInfo) item));
            }
        }
        return files;
    }

    private List<Path> resources() {
        List<FileInfo> items = fileItems();
        List<Path> files = new ArrayList<>(items.size());
        for (FileInfo item : items) {
            files.add(item.selfPath());
        }
        return files;
    }

    UiFileActivity assertAllItemsDisplayedInOrder(final Path... expected) {
        awaitOnMainThread(instrument, new Runnable() {
            @Override
            public void run() {
                List<FileInfo> items = fileItems();
                List<Path> actual = new ArrayList<>(items.size());
                for (FileInfo item : items) {
                    actual.add(item.selfPath());
                }

                if (!asList(expected).equals(actual)) {
                    throw new AssertionError("" +
                            "\nexpected in order:\n" + TextUtils.join("\n", expected) +
                            "\nbus was:\n" + TextUtils.join("\n", actual));
                }
            }
        });
        return this;
    }

    public UiInfo getInfo() {
        selectActionModeAction(R.id.info);
        return new UiInfo(this);
    }
}
