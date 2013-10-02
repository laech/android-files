package l.files.features;

import l.files.test.BaseFilesActivityTest;

import java.io.File;

import static l.files.app.UserDirs.DIR_HOME;

public final class TabTest extends BaseFilesActivityTest {

    public void testOpensNewTab() {
        screen().openNewTab()
                .assertTabCount(2);
    }

    public void testOpeningNewTabWillCloseOpenedDrawer() {
        screen().openDrawer()
                .openNewTab()
                .assertDrawerIsOpened(false);
    }

    public void testClosingTabWillCloseOpenedDrawer() {
        screen().openNewTab()
                .openDrawer()
                .closeCurrentTab()
                .assertTabCount(1)
                .assertDrawerIsOpened(false);
    }

    public void testSelectsNewlyOpenedTab() {
        screen().openNewTab()
                .assertSelectedTabPosition(1);
    }

    public void testNewTabOpensInHomeFolder() {
        screen().selectItem(dir().newDir())
                .openNewTab()
                .assertCurrentDirectory(DIR_HOME);
    }

    public void testEachTabShowsItsOwnContent() {
        final File directory = dir().newDir();
        screen().selectItem(directory)
                .openNewTab()
                .selectPage(0).assertCurrentDirectory(directory)
                .selectPage(1).assertCurrentDirectory(DIR_HOME);
    }

    public void testShowsBackIndicatorIfCanGoBack() {
        screen().selectItem(dir().newDir())
                .assertTabBackIndicatorVisibleAt(0, false);
    }

    public void testHidesBackIndicatorIfCanNotGoBack() {
        screen().selectItem(dir().newDir())
                .pressBack()
                .assertTabBackIndicatorVisibleAt(0, false);
    }

    public void testHidesBackIndicatorIfTabIsNotCurrentlySelected() {
        screen().selectItem(dir().newDir())
                .openNewTab()
                .selectPage(1)
                .assertTabBackIndicatorVisibleAt(0, false);
    }

    public void testShowsDirectoryNameAsTabTitle() {
        final File dir = dir().newDir();
        screen().selectItem(dir)
                .assertTabTitleAt(0, dir.getName());
    }

    public void testTabIsHighlightedIfSelected() {
        screen().assertTabHighlightedAt(0, true);
    }

    public void testTabIsNotHighlightedIfNotSelected() {
        screen().openNewTab()
                .selectPage(0)
                .assertTabHighlightedAt(1, false);
    }

    public void testPressingBackClosesTabIfNoMoreBackStack() {
        screen().openNewTab().assertTabCount(2)
                .pressBack().assertTabCount(1);
    }

    public void testClickingOnUnselectedTabWillSwitchToIt() {
        final File dir = dir().newDir();
        screen().selectItem(dir)
                .openNewTab()
                .selectTabAt(0)
                .assertCurrentDirectory(dir);
    }

    public void testClickingOnCurrentTabWillOpenDrawerIfThereIsNoBackStack() {
        screen().assertDrawerIsOpened(false)
                .selectTabAt(0)
                .assertDrawerIsOpened(true);
    }

    public void testClickingOnCurrentTabWillCloseDrawerIfDrawerIsOpenedAndThereIsNoBackStack() {
        screen().selectTabAt(0)
                .assertDrawerIsOpened(true)
                .selectTabAt(0)
                .assertDrawerIsOpened(false);
    }

    public void testClickingOnCurrentTabWillCloseDrawerIfDrawerIsOpenedAndThereIsBackStack() {
        screen().selectItem(dir().newDir())
                .assertTabBackIndicatorVisibleAt(0, true)
                .openDrawer()
                .assertDrawerIsOpened(true)
                .selectTabAt(0)
                .assertDrawerIsOpened(false);
    }

    public void testClickingOnCurrentTabWillGoBackIfThereIsBackStack() {
        final File dir = dir().newDir();
        screen().selectItem(dir).assertCurrentDirectory(dir)
                .selectTabAt(0).assertCurrentDirectory(dir().get());
    }
}
