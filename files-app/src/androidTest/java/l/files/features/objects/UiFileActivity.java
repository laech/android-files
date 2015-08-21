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

import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.base.Provider;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.ui.FileLabels;
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
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.test.Mocks.mockMenuItem;

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

  public UiFileActivity selectFromNavigationMode(final Resource dir) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        int position = activity().hierarchy().indexOf(dir);
        activity().title().setSelection(position);
      }
    });
    return this;
  }

  public UiFileActivity clickInto(Resource resource) {
    click(resource);
    assertCurrentDirectory(resource);
    return this;
  }

  public UiFileActivity click(Resource resource) {
    clickItemOnMainThread(instrument, recycler(), resource);
    return this;
  }

  public UiFileActivity longClick(Resource resource) {
    longClickItemOnMainThread(instrument, recycler(), resource);
    return this;
  }

  public UiBookmarksFragment openBookmarksDrawer() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        activity().drawerLayout().openDrawer(START);
      }
    });
    assertDrawerIsOpened(true);
    return new UiBookmarksFragment(this);
  }

  public UiFileActivity assertDrawerIsOpened(final boolean opened) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(opened, activity().drawerLayout().isDrawerOpen(START));
      }
    });
    return this;
  }

  public UiFileActivity pressBack() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        // This is to wait for existing messages on the main thread
        // queue to be cleared first
      }
    });
    instrument.sendKeyDownUpSync(KEYCODE_BACK);
    return this;
  }

  public UiFileActivity longPressBack() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(activity().onKeyLongPress(KEYCODE_BACK, null));
      }
    });
    return this;
  }

  public UiFileActivity pressActionBarUpIndicator() {
    waitForUpIndicatorToAppear();
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        MenuItem item = mockMenuItem(android.R.id.home);
        assertTrue(activity().onOptionsItemSelected(item));
      }
    });
    return this;
  }

  public void waitForUpIndicatorToAppear() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
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
      @Override public void apply(MenuItem input) {
        assertEquals(can, input.isEnabled());
      }
    });
  }

  private UiFileActivity findOptionMenuItem(
      final int id,
      final Consumer<MenuItem> consumer) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        activity().toolbar().hideOverflowMenu();
        activity().toolbar().showOverflowMenu();
      }
    });
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        MenuItem item = activity().toolbar().getMenu().findItem(id);
        assertNotNull(item);
        consumer.apply(item);
      }
    });
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        activity().toolbar().hideOverflowMenu();
      }
    });
    return this;
  }

  public UiFileActivity assertCurrentDirectory(final Resource expected) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        FilesFragment fragment = activity().fragment();
        assertNotNull(fragment);
        Resource actual = fragment.directory();
        assertEquals(expected, actual);
      }
    });
    return this;
  }

  public UiFileActivity assertListViewContains(
      final Resource item,
      final boolean contains) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(contains, resources().contains(item));
      }
    });
    return this;
  }

  public UiFileActivity assertSummaryView(
      final Resource resource,
      final Consumer<CharSequence> assertion) {
    findItemOnMainThread(resource, new Consumer<View>() {
      @Override public void apply(View input) {
        TextView summary = find(R.id.summary, input);
        assertion.apply(summary.getText());
      }
    });
    return this;
  }

  public UiFileActivity assertActionBarTitle(final String title) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(title, label((Resource) activity().title().getSelectedItem()));
      }
    });
    return this;
  }

  private String label(Resource res) {
    return FileLabels.get(activity().getResources(), res);
  }

  public UiFileActivity assertActionBarUpIndicatorIsVisible(
      final boolean visible) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
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
      Resource resource,
      Consumer<View> consumer) {
    Instrumentations.findItemOnMainThread(
        instrument, recycler(), resource, consumer);
  }

  private RecyclerView recycler() {
    return awaitOnMainThread(instrument, new Callable<RecyclerView>() {
      @Override public RecyclerView call() throws Exception {
        return fragment().recycler;
      }
    });
  }

  private MenuItem renameMenu() {
    return activity().currentActionMode().getMenu().findItem(R.id.rename);
  }

  private UiFileActivity selectMenuAction(int id) {
    findOptionMenuItem(id, new Consumer<MenuItem>() {
      @Override public void apply(MenuItem item) {
        assertTrue(item.isEnabled());
      }
    });
    instrument.invokeMenuActionSync(activity(), id, 0);
    return this;
  }

  void selectActionModeAction(final int id) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
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
      @Override public void run() {
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
      Resource resource, final boolean checked) {
    findItemOnMainThread(resource, new Consumer<View>() {
      @Override public void apply(View view) {
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
      @Override public void run() {
        assertEquals(present, activity().currentActionMode() != null);
      }
    });
    return this;
  }

  public UiFileActivity assertActionModeTitle(final Object title) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        ActionMode mode = activity().currentActionMode();
        assertNotNull(mode);
        assertEquals(title.toString(), mode.getTitle().toString());
      }
    });
    return this;
  }

  public UiFileActivity assertBookmarkMenuChecked(final boolean checked) {
    return findOptionMenuItem(R.id.bookmark, new Consumer<MenuItem>() {
      @Override public void apply(MenuItem item) {
        assertEquals(checked, item.isChecked());
      }
    });
  }

  public UiFileActivity assertSymbolicLinkIconDisplayed(
      Resource resource,
      final boolean displayed) {
    findItemOnMainThread(resource, new Consumer<View>() {
      @Override public void apply(View input) {
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
      @Override public void run() {
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
      @Override public void run() {
        assertEquals(false, activity().drawerLayout().isDrawerOpen(START));
      }
    });
    return this;
  }

  public UiFileActivity assertDisabled(Resource resource) {
    findItemOnMainThread(resource, new Consumer<View>() {
      @Override public void apply(View input) {
        assertFalse(input.findViewById(R.id.icon).isEnabled());
        assertFalse(input.findViewById(R.id.title).isEnabled());
        assertFalse(input.findViewById(R.id.summary).isEnabled());
      }
    });
    return this;
  }

  public UiFileActivity assertNavigationModeHierarchy(final Resource dir) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        List<Resource> actual = activity().hierarchy();
        List<Resource> expected = new ArrayList<>(dir.hierarchy());
        reverse(expected);
        assertEquals(expected, actual);
        assertEquals(dir, activity().title().getSelectedItem());
      }
    });
    return this;
  }

  public UiFileActivity assertListViewContainsChildrenOf(
      final Resource dir) throws IOException {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(
            childrenStatsSortedByPath(dir),
            listViewStatsSortedByPath());
      }
    });
    return this;
  }

  private List<Pair<Resource, Stat>> childrenStatsSortedByPath(Resource dir) {
    try {
      List<Resource> children = sortResourcesByPath(dir.list(NOFOLLOW));
      return stat(children);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<Pair<Resource, Stat>> stat(
      List<Resource> resources) throws IOException {
    List<Pair<Resource, Stat>> result = new ArrayList<>();
    for (Resource resource : resources) {
      result.add(Pair.create(resource, resource.stat(NOFOLLOW)));
    }
    return result;
  }

  private List<Resource> sortResourcesByPath(List<Resource> resources) {
    Collections.sort(resources, new Comparator<Resource>() {
      @Override public int compare(Resource a, Resource b) {
        return a.path().compareTo(b.path());
      }
    });
    return resources;
  }

  private List<Pair<Resource, Stat>> listViewStatsSortedByPath() {
    List<FileListItem.File> items = sortFilesByPath(fileItems());
    return stats(items);
  }

  private List<Pair<Resource, Stat>> stats(List<FileListItem.File> items) {
    List<Pair<Resource, Stat>> result = new ArrayList<>();
    for (FileListItem.File item : items) {
      result.add(Pair.create(item.resource(), item.stat()));
    }
    return result;
  }

  private List<FileListItem.File> sortFilesByPath(
      List<FileListItem.File> items) {
    Collections.sort(items, new Comparator<FileListItem.File>() {
      @Override public int compare(
          FileListItem.File a,
          FileListItem.File b) {
        return a.resource().path().compareTo(b.resource().path());
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

  private List<Resource> resources() {
    List<FileListItem.File> items = fileItems();
    List<Resource> resources = new ArrayList<>(items.size());
    for (FileListItem.File item : items) {
      resources.add(item.resource());
    }
    return resources;
  }

  public UiFileActivity assertItemsDisplayed(final Resource... expected) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        List<Resource> actual = new ArrayList<>();
        for (FileListItem.File item : fileItems()) {
          actual.add(item.resource());
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
      @Override public void run() {
        activity().setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
        activity().setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        activity().setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
      }
    });

    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        // This is waiting for previous action to be cleared in the UI thread
      }
    });

    activity = (FilesActivity) monitor.getLastActivity();
    assertNotNull(activity);
    instrument.removeMonitor(monitor);
    return this;
  }
}
