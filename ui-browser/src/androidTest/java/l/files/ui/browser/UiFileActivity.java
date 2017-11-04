package l.files.ui.browser;

import android.app.Instrumentation;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import android.support.annotation.Nullable;

import l.files.base.Consumer;
import l.files.base.Provider;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.fs.FileLabels;

import static android.support.v4.view.GravityCompat.START;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.ui.browser.Instrumentations.await;
import static l.files.ui.browser.Instrumentations.awaitOnMainThread;
import static l.files.ui.browser.Instrumentations.clickItemOnMainThread;
import static l.files.ui.browser.Instrumentations.longClickItemOnMainThread;

final class UiFileActivity {

    private final Instrumentation instrument;
    private final Provider<FilesActivity> activity;

    UiFileActivity(
            Instrumentation instrumentation,
            Provider<FilesActivity> provider
    ) {
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

    UiFileActivity selectFromNavigationMode(Path dir) {
        awaitOnMainThread(instrument, () -> {
            int position = activity().hierarchy().indexOf(dir);
            activity().title().setSelection(position);
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
        awaitOnMainThread(instrument, () -> activity().drawerLayout().openDrawer(START));
        assertDrawerIsOpened(true);
        return new UiBookmarksFragment(this);
    }

    UiFileActivity assertDrawerIsOpened(boolean opened) {
        awaitOnMainThread(instrument, () -> assertEquals(opened, activity().drawerLayout().isDrawerOpen(START)));
        return this;
    }

    UiFileActivity pressBack() {
        instrument.waitForIdleSync();
        instrument.sendKeyDownUpSync(KEYCODE_BACK);
        instrument.waitForIdleSync();
        return this;
    }

    UiFileActivity longPressBack() {
        awaitOnMainThread(instrument, () -> assertTrue(activity().onKeyLongPress(KEYCODE_BACK, null)));
        return this;
    }

    UiFileActivity pressActionBarUpIndicator() {
        waitForUpIndicatorToAppear();
        awaitOnMainThread(instrument, () -> {
            MenuItem item = new TestMenuItem(android.R.id.home);
            assertTrue(activity().onOptionsItemSelected(item));
        });
        return this;
    }

    private void waitForUpIndicatorToAppear() {
        awaitOnMainThread(instrument, () -> assertEquals(1F, activity().navigationIcon().getProgress()));
    }

    UiFileActivity assertCanRename(boolean can) {
        assertEquals(can, renameMenu().isEnabled());
        return this;
    }

    UiFileActivity assertCanPaste(boolean can) {
        return findOptionMenuItem(android.R.id.paste, input -> {
            String msg = "Paste menu enabled to be " + can;
            assertEquals(msg, can, input.isEnabled());
        });
    }

    private UiFileActivity findOptionMenuItem(int id, Consumer<MenuItem> consumer) {

        awaitOnMainThread(instrument, () -> {
            Toolbar toolbar = activity().toolbar();
            toolbar.hideOverflowMenu();
            toolbar.showOverflowMenu();
            MenuItem item = toolbar.getMenu().findItem(id);
            assertNotNull(item);
            consumer.accept(item);
            toolbar.hideOverflowMenu();
        });
        return this;
    }

    private UiFileActivity clickOptionMenuItem(int id) {
        awaitOnMainThread(instrument, (Runnable) () -> activity().toolbar().getMenu().performIdentifierAction(id, 0));
        return this;
    }

    UiFileActivity assertCurrentDirectory(Path expected) {
        awaitOnMainThread(instrument, () -> {
            FilesFragment fragment = activity().fragment();
            assertNotNull(fragment);
            Path actual = fragment.directory();
            assertEquals(expected, actual);
        });
        return this;
    }

    UiFileActivity assertListViewContains(Path item, boolean contains) {
        awaitOnMainThread(instrument, () -> assertEquals(contains, resources().contains(item)));
        return this;
    }

    UiFileActivity assertActionBarTitle(String title) {
        awaitOnMainThread(instrument, () -> assertEquals(title, label((Path) activity().title().getSelectedItem())));
        return this;
    }

    private String label(Path file) {
        return FileLabels.get(activity().getResources(), file);
    }

    UiFileActivity assertActionBarUpIndicatorIsVisible(boolean visible) {
        awaitOnMainThread(instrument, () -> assertEquals(visible ? 1F : 0F, activity().navigationIcon().getProgress()));
        return this;
    }

    private void findItemOnMainThread(
            Path file,
            Consumer<View> consumer) {
        Instrumentations.findItemOnMainThread(
                instrument, recycler(), file, consumer);
    }

    private Provider<RecyclerView> recycler() {
        return () -> awaitOnMainThread(instrument, () -> fragment().recycler);
    }

    private MenuItem renameMenu() {
        return activity().currentActionMode().getMenu().findItem(R.id.rename);
    }

    private UiFileActivity selectMenuAction(int id) {
        findOptionMenuItem(id, item -> assertTrue(item.isEnabled()));
        return clickOptionMenuItem(id);
    }

    void selectActionModeAction(int id) {
        awaitOnMainThread(instrument, () -> {
            ActionMode mode = activity().currentActionMode();
            assertNotNull(mode);
            MenuItem item = mode.getMenu().findItem(id);
            //noinspection ConstantConditions
            assertTrue(activity()
                    .currentActionModeCallback()
                    .onActionItemClicked(mode, item));
        });
    }

    void waitForActionModeToFinish() {
        awaitOnMainThread(instrument, () -> assertNull(activity().currentActionMode()));
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
    UiFileActivity assertChecked(Path file, boolean checked) {
        findItemOnMainThread(file, view -> assertEquals(checked, view.isActivated()));
        return this;
    }

    /**
     * Asserts whether the activity.get() currently in an action mode.
     */
    UiFileActivity assertActionModePresent(boolean present) {
        awaitOnMainThread(instrument, () -> assertEquals(present, activity().currentActionMode() != null));
        return this;
    }

    UiFileActivity assertActionModeTitle(Object title) {
        awaitOnMainThread(instrument, () -> {
            ActionMode mode = activity().currentActionMode();
            assertNotNull(mode);
            assertEquals(title.toString(), mode.getTitle().toString());
        });
        return this;
    }

    UiFileActivity assertBookmarkMenuChecked(boolean checked) {
        return findOptionMenuItem(R.id.bookmark, item -> assertEquals(checked, item.isChecked()));
    }

    UiFileActivity assertRefreshMenuVisible(boolean visible) {
        return findOptionMenuItem(R.id.refresh, input -> {
            String msg = "Refresh menu visible to be " + visible;
            assertEquals(msg, visible, input.isVisible());
        });
    }

    UiFileActivity assertThumbnailShown(Path path, boolean shown) {
        findItemOnMainThread(path, view -> {
            ImageView imageView = view.findViewById(R.id.image);
            Drawable drawable = imageView.getDrawable();
            assertEquals(shown, drawable instanceof BitmapDrawable
                    || drawable instanceof RoundedBitmapDrawable);
        });
        return this;
    }

    UiFileActivity assertLinkPathDisplayed(Path link, @Nullable Path target) {

        findItemOnMainThread(link, view -> {
            TextView linkView = view.findViewById(R.id.link);
            if (target != null) {
                Resources res = view.getResources();
                String expected = res.getString(R.string.link_x, target);
                CharSequence actual = linkView.getText().toString();
                assertEquals(expected, actual);
                assertEquals(VISIBLE, linkView.getVisibility());
            } else {
                assertEquals(GONE, linkView.getVisibility());
            }
        });

        return this;
    }

    UiFileActivity assertSummary(Path path, CharSequence expected) {
        return assertSummary(path, summary -> assertEquals(expected, summary));
    }

    UiFileActivity assertSummary(
            Path path,
            Consumer<String> assertion
    ) {
        findItemOnMainThread(path, view -> {
            TextView summaryView = view.findViewById(R.id.summary);
            assertion.accept(summaryView.getText().toString());
        });
        return this;
    }

    UiFileActivity assertBookmarksSidebarIsClosed() {
        awaitOnMainThread(instrument, () -> assertEquals(false, activity().drawerLayout().isDrawerOpen(START)));
        return this;
    }

    UiFileActivity assertDisabled(Path path) {
        findItemOnMainThread(path, view -> {
            assertFalse(view.findViewById(R.id.title).isEnabled());
            assertFalse(view.findViewById(R.id.summary).isEnabled());
            assertFalse(view.findViewById(R.id.link).isEnabled());
        });
        return this;
    }

    UiFileActivity assertNavigationModeHierarchy(Path dir) {
        awaitOnMainThread(instrument, () -> {
            List<Path> actual = activity().hierarchy();
            List<Path> expected = new ArrayList<>(dir.hierarchy());
            Collections.reverse(expected);
            assertEquals(expected, actual);
            assertEquals(dir, activity().title().getSelectedItem());
        });
        return this;
    }

    UiFileActivity assertListMatchesFileSystem(Path dir)
            throws IOException {
        return assertListMatchesFileSystem(dir, 1, MINUTES);
    }

    UiFileActivity assertListMatchesFileSystem(
            Path dir,
            int timeout,
            TimeUnit timeoutUnit
    ) throws IOException {

        await((Callable<Void>) () -> {


            SimpleArrayMap<Path, Stat> filesInView = filesInView();

            dir.list((Path.Consumer) child -> {
                Stat oldStat = filesInView.remove(child);
                if (oldStat == null) {
                    fail("Path in file system but not in view: " + child);
                }

                Stat newStat = child.stat(NOFOLLOW);
                if (!newStat.equals(oldStat)) {
                    fail("Path details differ for : " + child
                            + "\nnew: " + newStat
                            + "\nold: " + oldStat);
                }
                return true;
            });

            if (!filesInView.isEmpty()) {
                fail("Path in view but not on file system: "
                        + filesInView.keyAt(0) + "="
                        + filesInView.valueAt(0));
            }

            return null;

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

    UiFileActivity assertAllItemsDisplayedInOrder(Path... expected) {
        awaitOnMainThread(instrument, () -> {
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
        });
        return this;
    }

    UiInfo getInfo() {
        selectActionModeAction(R.id.info);
        return new UiInfo(this);
    }
}
