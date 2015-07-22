package l.files.features.objects;

import android.app.Instrumentation;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Pair;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.ui.FileLabels;
import l.files.ui.bookmarks.BookmarksFragment;
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
  private final FilesActivity activity;

  public UiFileActivity(
      Instrumentation instrumentation,
      FilesActivity activity) {
    this.instrument = instrumentation;
    this.activity = activity;
  }

  private FilesFragment fragment() {
    return (FilesFragment) activity
        .getFragmentManager()
        .findFragmentByTag(FilesFragment.TAG);
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
    return new UiNewDir(instrument, activity);
  }

  public UiRename rename() {
    selectActionModeAction(R.id.rename);
    return new UiRename(instrument, activity);
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
    return new UiSort(instrument, activity);
  }

  public UiFileActivity selectFromNavigationMode(final Resource dir) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        int position = activity.hierarchy().indexOf(dir);
        activity.title().setSelection(position);
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
    clickItemOnMainThread(instrument, fragment().recycler, resource);
    return this;
  }

  public UiFileActivity longClick(Resource resource) {
    longClickItemOnMainThread(instrument, fragment().recycler, resource);
    return this;
  }

  public UiBookmarksFragment openBookmarksDrawer() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        activity.drawerLayout().openDrawer(START);
      }
    });
    assertDrawerIsOpened(true);
    return new UiBookmarksFragment(instrument, (BookmarksFragment) activity
        .getFragmentManager().findFragmentById(R.id.bookmarks_fragment));
  }

  public UiFileActivity assertDrawerIsOpened(final boolean opened) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(opened, activity.drawerLayout().isDrawerOpen(START));
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
        assertTrue(activity.onKeyLongPress(KEYCODE_BACK, null));
      }
    });
    return this;
  }

  public UiFileActivity pressActionBarUpIndicator() {
    waitForUpIndicatorToAppear();
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        MenuItem item = mockMenuItem(android.R.id.home);
        assertTrue(activity.onOptionsItemSelected(item));
      }
    });
    return this;
  }

  public void waitForUpIndicatorToAppear() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(!activity.drawerToggle().isDrawerIndicatorEnabled());
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
        activity.toolbar().hideOverflowMenu();
        activity.toolbar().showOverflowMenu();
      }
    });
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        MenuItem item = activity.toolbar().getMenu().findItem(id);
        assertNotNull(item);
        consumer.apply(item);
      }
    });
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        activity.toolbar().hideOverflowMenu();
      }
    });
    return this;
  }

  public UiFileActivity assertCurrentDirectory(final Resource expected) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        FilesFragment fragment = activity.fragment();
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
        assertEquals(title, label((Resource) activity.title().getSelectedItem()));
      }
    });
    return this;
  }

  private String label(Resource res) {
    return FileLabels.get(activity.getResources(), res);
  }

  public UiFileActivity assertActionBarUpIndicatorIsVisible(
      final boolean visible) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        ActionBarDrawerToggle toggle = activity.drawerToggle();
        assertEquals(visible, !toggle.isDrawerIndicatorEnabled());
      }
    });
    return this;
  }

  private void findItemOnMainThread(
      Resource resource,
      Consumer<View> consumer) {
    Instrumentations.findItemOnMainThread(
        instrument, fragment().recycler, resource, consumer);
  }

  private MenuItem renameMenu() {
    return activity.currentActionMode().getMenu().findItem(R.id.rename);
  }

  private UiFileActivity selectMenuAction(int id) {
    findOptionMenuItem(id, new Consumer<MenuItem>() {
      @Override public void apply(MenuItem item) {
        assertTrue(item.isEnabled());
      }
    });
    instrument.invokeMenuActionSync(activity, id, 0);
    return this;
  }

  void selectActionModeAction(final int id) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        ActionMode mode = activity.currentActionMode();
        MenuItem item = mode.getMenu().findItem(id);
        assertTrue(activity
            .currentActionModeCallback()
            .onActionItemClicked(mode, item));
      }
    });
  }

  void waitForActionModeToFinish() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertNull(activity.currentActionMode());
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
   * Asserts whether the activity currently in an action mode.
   */
  public UiFileActivity assertActionModePresent(final boolean present) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(present, activity.currentActionMode() != null);
      }
    });
    return this;
  }

  public UiFileActivity assertActionModeTitle(final Object title) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        ActionMode mode = activity.currentActionMode();
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
            LOCK_MODE_LOCKED_OPEN == activity.drawerLayout()
                .getDrawerLockMode(START));
      }
    });
    return this;
  }

  public UiFileActivity assertBookmarksSidebarIsClosed() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(false, activity.drawerLayout().isDrawerOpen(START));
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
        List<Resource> actual = activity.hierarchy();
        List<Resource> expected = new ArrayList<Resource>(dir.hierarchy());
        reverse(expected);
        assertEquals(expected, actual);
        assertEquals(dir, activity.title().getSelectedItem());
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

  private List<Pair<Resource, Stat>> stat(List<Resource> resources)
      throws IOException {
    return Lists.transform(resources,
        new Function<Resource, Pair<Resource, Stat>>() {
          @Override public Pair<Resource, Stat> apply(Resource input) {
            try {
              return Pair.create(input, input.stat(NOFOLLOW));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        });
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
    ImmutableList.Builder<Pair<Resource, Stat>> b = ImmutableList.builder();
    for (FileListItem.File item : items) {
      b.add(Pair.create(item.resource(), item.stat()));
    }
    return b.build();
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
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        activity.setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
        activity.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
      }
    });
    return this;
  }
}
