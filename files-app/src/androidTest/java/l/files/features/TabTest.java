package l.files.features;

import java.io.File;

import l.files.test.BaseFilesActivityTest;

import static l.files.ui.UserDirs.DIR_HOME;

public final class TabTest extends BaseFilesActivityTest {

  public void testOpensNewTab() {
    screen()
        .openNewTab()
        .assertTabCount(2);
  }

  public void testClosesTab() {
    final File dir = dir().createDir("a");
    screen()
        .selectItem(dir)
        .openNewTab()
        .openNewTab()
        .assertTabCount(3)
        .closeCurrentTab()
        .assertTabCount(2)
        .closeCurrentTab()
        .assertTabCount(1)
        .assertCurrentDirectory(dir);
  }

  public void testSelectsNewlyOpenedTab() {
    screen()
        .openNewTab()
        .assertSelectedTabPosition(1);
  }

  public void testNewTabOpensInHomeFolder() {
    screen()
        .selectItem(dir().createDir("a"))
        .openNewTab()
        .assertCurrentDirectory(DIR_HOME);
  }

  public void testEachTabShowsItsOwnContent() {
    File directory = dir().createDir("a");
    screen()
        .selectItem(directory)
        .openNewTab()
        .selectPage(0).assertCurrentDirectory(directory)
        .selectPage(1).assertCurrentDirectory(DIR_HOME);
  }

  public void testShowsBackIndicatorIfCanGoBack() {
    screen()
        .openNewTab()
        .selectPage(0)
        .selectItem(dir().createDir("a"))
        .assertTabBackIndicatorVisibleAt(0, true);
  }

  public void testHidesBackIndicatorIfCanNotGoBack() {
    screen()
        .selectItem(dir().createDir("a"))
        .pressBack()
        .assertTabBackIndicatorVisibleAt(0, false);
  }

  public void testHidesBackIndicatorIfTabIsNotCurrentlySelected() {
    screen()
        .selectItem(dir().createDir("a"))
        .openNewTab()
        .selectPage(1)
        .assertTabBackIndicatorVisibleAt(0, false);
  }

  public void testShowsDirectoryNameAsTabTitle() {
    File dir = dir().createDir("a");
    screen()
        .selectItem(dir)
        .assertTabTitleAt(0, dir.getName());
  }

  public void testTabIsHighlightedIfSelected() {
    screen().assertTabHighlightedAt(0, true);
  }

  public void testTabIsNotHighlightedIfNotSelected() {
    screen()
        .openNewTab()
        .selectPage(0)
        .assertTabHighlightedAt(1, false);
  }

  public void testPressingBackClosesTabIfNoMoreBackStack() {
    screen()
        .openNewTab().assertTabCount(2)
        .pressBack().assertTabCount(1);
  }

  public void testClickingOnUnselectedTabWillSwitchToIt() {
    File dir = dir().createDir("a");
    screen()
        .selectItem(dir)
        .openNewTab()
        .selectTabAt(0)
        .assertCurrentDirectory(dir);
  }

  public void testClickingOnCurrentTabWillOpenDrawerIfThereIsNoBackStack() {
    screen()
        .assertDrawerIsOpened(false)
        .selectTabAt(0)
        .assertDrawerIsOpened(true);
  }

  public void testClickingOnCurrentTabWillCloseDrawerIfDrawerIsOpenedAndThereIsNoBackStack() {
    screen()
        .selectTabAt(0)
        .assertDrawerIsOpened(true)
        .selectTabAt(0)
        .assertDrawerIsOpened(false);
  }

  public void testClickingOnCurrentTabWillCloseDrawerIfDrawerIsOpenedAndThereIsBackStack() {
    screen()
        .selectItem(dir().createDir("a"))
        .assertTabBackIndicatorVisibleAt(0, true)
        .openBookmarksDrawer()
        .getActivityObject()
        .assertDrawerIsOpened(true)
        .selectTabAt(0)
        .assertDrawerIsOpened(false);
  }

  public void testClickingOnCurrentTabWillGoBackIfThereIsBackStack() {
    File dir = dir().createDir("a");
    screen()
        .selectItem(dir).assertCurrentDirectory(dir)
        .selectTabAt(0).assertCurrentDirectory(dir().get());
  }

  public void testHidesTabBarIfThereIsOnlyOneTabInitially() {
    screen()
        .assertTabCount(1)
        .assertTabBarIsVisible(false);
  }

  public void testShowsTabBarIfThereIsMoreThanOneTab() {
    screen()
        .openNewTab()
        .assertTabCount(2)
        .assertTabBarIsVisible(true);
  }

  public void testHidesTabBarIfTabsAreClosedWithOneLeft() {
    screen()
        .selectItem(dir().createDir("a"))
        .openNewTab()
        .assertTabCount(2)
        .closeCurrentTab()
        .assertTabBarIsVisible(false);
  }
}
