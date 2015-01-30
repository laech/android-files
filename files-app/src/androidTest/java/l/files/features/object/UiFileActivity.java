package l.files.features.object;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Instrumentation;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import l.files.R;
import l.files.common.base.Consumer;
import l.files.fs.FileStatus;
import l.files.fs.Path;
import l.files.fs.local.LocalPath;
import l.files.ui.FilesActivity;
import l.files.ui.FilesPagerFragment;

import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;
import static android.app.ActionBar.DISPLAY_SHOW_HOME;
import static android.app.ActionBar.DISPLAY_SHOW_TITLE;
import static android.test.MoreAsserts.assertNotEqual;
import static android.view.View.VISIBLE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static l.files.features.object.Instrumentations.await;
import static l.files.features.object.Instrumentations.awaitOnMainThread;
import static l.files.test.Mocks.mockMenuItem;

public final class UiFileActivity {

  private final Instrumentation instrument;
  private final FilesActivity activity;

  public UiFileActivity(Instrumentation in, FilesActivity activity) {
    this.instrument = in;
    this.activity = activity;
  }

  public UiFileActivity bookmark() {
    assertBookmarkMenuChecked(false);
    await(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(instrument.invokeMenuActionSync(activity, R.id.bookmark, 0));
      }
    });
    return this;
  }

  public UiFileActivity unbookmark() {
    assertBookmarkMenuChecked(true);
    await(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(instrument.invokeMenuActionSync(activity, R.id.bookmark, 0));
      }
    });
    return this;
  }

  public UiFileActivity check(final File file, final boolean checked) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        getListView().setItemChecked(findItemPositionOrThrow(file.getName()), checked);
      }
    });
    return this;
  }

  public UiNewFolder newFolder() {
    await(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(instrument.invokeMenuActionSync(activity, R.id.new_dir, 0));
      }
    });
    return new UiNewFolder(instrument, activity);
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
    await(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(instrument.invokeMenuActionSync(
            activity, android.R.id.paste, 0));
      }
    });
    return this;
  }

  public UiFileActivity selectItem(final File file) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        ListView list = getListView();
        int position = findItemPositionOrThrow(file.getName());
        int firstVisiblePosition = list.getFirstVisiblePosition();
        int viewPosition = position - firstVisiblePosition;
        View view = list.getChildAt(viewPosition);
        assertTrue(list.performItemClick(view, viewPosition, position));
      }
    });
    if (file.isDirectory()) {
      assertCurrentDirectory(file);
    }
    return this;
  }

  public UiFileActivity selectPage(final int position) {
    return awaitOnMainThread(instrument, new Callable<UiFileActivity>() {
      @Override public UiFileActivity call() {
        activity.getViewPager().setCurrentItem(position, false);
        return UiFileActivity.this;
      }
    });
  }

  public UiFileActivity selectTabAt(final int position) {
    return awaitOnMainThread(instrument, new Callable<UiFileActivity>() {
      @Override public UiFileActivity call() throws Exception {
        assertTrue(activity.getViewPagerTabBar().getTabAt(position).getRootView().performClick());
        return UiFileActivity.this;
      }
    });
  }

  public UiFileActivity openNewTab() {
    await(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(instrument.invokeMenuActionSync(activity, R.id.new_tab, 0));
      }
    });
    return this;
  }

  public UiFileActivity closeCurrentTab() {
    await(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(instrument.invokeMenuActionSync(activity, R.id.close_tab, 0));
      }
    });
    return this;
  }

  public UiFileActivity openDrawer() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        activity.getDrawerLayout().openDrawer(Gravity.START);
      }
    });
    return assertDrawerIsOpened(true);
  }

  public UiFileActivity pressBack() {
    return awaitOnMainThread(instrument, new Callable<UiFileActivity>() {
      @Override public UiFileActivity call() throws Exception {
        activity.onBackPressed();
        return UiFileActivity.this;
      }
    });
  }

  public UiFileActivity longPressBack() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertTrue(activity.onKeyLongPress(KeyEvent.KEYCODE_BACK, null));
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
        assertTrue(!activity.getActionBarDrawerToggle().isDrawerIndicatorEnabled());
      }
    });
  }

  public UiFileActivity assertCanRename(final boolean can) {
    assertEquals(can, getRenameMenuItem().isEnabled());
    return this;
  }

  public UiFileActivity assertTabCount(final int count) {
    awaitOnMainThread(instrument, new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return activity.getViewPagerTabBar().getTabCount() == count;
      }
    });
    return this;
  }

  public UiFileActivity assertCurrentDirectory(final Path expected) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        FilesPagerFragment fragment = activity.getCurrentPagerFragment();
        Path actual = fragment.getCurrentPath();
        assertEquals(expected, actual);
      }
    });
    return this;
  }

  public UiFileActivity assertCurrentDirectory(File dir) {
    return assertCurrentDirectory(LocalPath.of(dir));
  }

  public UiFileActivity assertSelectedTabPosition(final int position) {
    awaitOnMainThread(instrument, new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return activity.getViewPager().getCurrentItem() == position;
      }
    });
    return this;
  }

  public UiFileActivity assertTabHighlightedAt(final int position, final boolean highlighted) {
    awaitOnMainThread(instrument, new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return highlighted == activity
            .getViewPagerTabBar()
            .getTabAt(position)
            .getRootView()
            .isSelected();
      }
    });
    return this;
  }

  public UiFileActivity assertTabBackIndicatorVisibleAt(final int position, final boolean visible) {
    awaitOnMainThread(instrument, new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return visible == (VISIBLE == activity
            .getViewPagerTabBar()
            .getTabAt(position)
            .getBackIndicatorView()
            .getVisibility());
      }
    });
    return this;
  }

  public UiFileActivity assertTabTitleAt(final int position, final String title) {
    awaitOnMainThread(instrument, new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return title.equals(activity
            .getViewPagerTabBar()
            .getTabAt(position)
            .getTitleView()
            .getText());
      }
    });
    return this;
  }

  public UiFileActivity assertDrawerIsOpened(final boolean opened) {
    awaitOnMainThread(instrument, new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return opened == activity.getDrawerLayout().isDrawerOpen(Gravity.START);
      }
    });
    return this;
  }

  public UiFileActivity assertListViewContains(final File item, final boolean contains) {
    awaitOnMainThread(instrument, new Callable<Boolean>() {
      @Override public Boolean call() {
        return contains == findItemPosition(item.getName()).isPresent();
      }
    });
    return this;
  }

  public UiFileActivity assertFileModifiedDateView(final File file, final Consumer<CharSequence> assertion) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        CharSequence actual = getFileModifiedView(file).getText();
        assertion.apply(actual);
      }
    });
    return this;
  }

  public UiFileActivity assertFileSizeView(final File file, final Consumer<CharSequence> assertion) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        CharSequence actual = getFileSizeView(file).getText();
        assertion.apply(actual);
      }
    });
    return this;
  }

  public UiFileActivity assertTabBarIsVisible(final boolean visible) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        ActionBar actionBar = activity.getActionBar();
        //noinspection ConstantConditions
        int showHome = actionBar.getDisplayOptions() & DISPLAY_SHOW_HOME;
        int showTitle = actionBar.getDisplayOptions() & DISPLAY_SHOW_TITLE;
        int showCustom = actionBar.getDisplayOptions() & DISPLAY_SHOW_CUSTOM;
        if (visible) {
          assertEquals(0, showHome);
          assertEquals(0, showTitle);
          assertNotEqual(0, showCustom);
        } else {
          assertNotEqual(0, showHome);
          assertNotEqual(0, showTitle);
          assertEquals(0, showCustom);
        }
      }
    });
    return this;
  }

  public UiFileActivity assertActionBarTitle(final String title) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        //noinspection ConstantConditions
        assertEquals(title, activity.getActionBar().getTitle());
      }
    });
    return this;
  }

  public UiFileActivity assertActionBarUpIndicatorIsVisible(final boolean visible) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(visible, !activity.getActionBarDrawerToggle().isDrawerIndicatorEnabled());
      }
    });
    return this;
  }

  private TextView getFileModifiedView(File file) {
    return (TextView) getView(file).findViewById(R.id.date);
  }

  private TextView getFileSizeView(File file) {
    return (TextView) getView(file).findViewById(R.id.size);
  }

  private View getView(File file) {
    ListView list = getListView();
    int index = findItemPositionOrThrow(file.getName());
    return list.getChildAt(index - list.getFirstVisiblePosition());
  }

  private ListView getListView() {
    //noinspection ConstantConditions
    return (ListView) activity
        .getCurrentPagerFragment()
        .getView()
        .findViewById(android.R.id.list);
  }

  private MenuItem getRenameMenuItem() {
    return activity.getCurrentActionMode().getMenu().findItem(R.id.rename);
  }

  private void selectActionModeAction(final int id) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        ActionMode mode = activity.getCurrentActionMode();
        MenuItem item = mode.getMenu().findItem(id);
        assertTrue(activity
            .getCurrentActionModeCallback()
            .onActionItemClicked(mode, item));
      }
    });
  }

  private void waitForActionModeToFinish() {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertNull(activity.getCurrentActionMode());
      }
    });
  }

  private int findItemPositionOrThrow(String filename) {
    Optional<Integer> position = findItemPosition(filename);
    if (position.isPresent()) {
      return position.get();
    }
    throw new AssertionError("No file with name: " + filename);
  }

  private Optional<Integer> findItemPosition(String filename) {
    int count = getListView().getCount();
    for (int i = 0; i < count; i++) {
      FileStatus stat = (FileStatus) getListView().getItemAtPosition(i);
      if (stat.name().equals(filename)) {
        return Optional.of(i);
      }
    }
    return Optional.absent();
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
  public UiFileActivity assertChecked(final File file, final boolean checked) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        int position = findItemPositionOrThrow(file.getName());
        assertEquals(checked, getListView().isItemChecked(position));
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
        assertEquals(present, activity.getCurrentActionMode() != null);
      }
    });
    return this;
  }

  public UiFileActivity assertBookmarkMenuChecked(final boolean checked) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        assertEquals(checked, activity.getMenu().findItem(R.id.bookmark).isChecked());
      }
    });
    return this;
  }

  public UiFileActivity assertBookmarkSidebarHasCurrentDirectory(final boolean has) {
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        Path path = activity.getCurrentPagerFragment().getCurrentPath();
        List<Path> paths = getSidebarBookmark();
        assertEquals(paths.toString(), has, paths.contains(path));
      }
    });
    return this;
  }

  public List<Path> getSidebarBookmark() {
    return getSidebarBookmarks(Functions.<Path>identity());
  }

  public List<String> getSidebarBookmarkNames() {
    return getSidebarBookmarks(new Function<Path, String>() {
      @Override public String apply(Path input) {
        return input.getName();
      }
    });
  }

  private <T> List<T> getSidebarBookmarks(final Function<Path, T> fn) {
    final List<T> result = new ArrayList<>();
    awaitOnMainThread(instrument, new Runnable() {
      @Override public void run() {
        FragmentManager manager = activity.getFragmentManager();
        activity.getDrawerLayout().openDrawer(Gravity.START);
        Fragment fragment = manager.findFragmentById(R.id.sidebar_fragment);
        ListView list = (ListView) fragment.getView();
        assertNotNull(list);
        for (int i = list.getHeaderViewsCount(); i < list.getCount(); i++) {
          Path path = (Path) list.getItemAtPosition(i);
          assertNotNull(path);
          result.add(fn.apply(path));
        }
      }
    });
    return result;
  }
}
