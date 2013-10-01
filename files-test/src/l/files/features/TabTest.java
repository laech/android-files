package l.files.features;

import l.files.test.BaseFilesActivityTest;

import java.io.File;

import static l.files.app.UserDirs.DIR_HOME;

public final class TabTest extends BaseFilesActivityTest {

    public void testOpensNewTab() {
        screen().openNewTab()
                .assertTabCount(2);
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
                .selectTab(0)
                .assertCurrentDirectory(dir);
    }

    public void testClickingOnCurrentTabWillDoNothingIfNoBackStack() {
        screen().selectTab(0)
                .assertCurrentDirectory(dir().get())
                .selectTab(0)
                .selectTab(0)
                .assertCurrentDirectory(dir().get());
    }

    public void testClickingOnCurrentTabWillGoBackIfThereIsBackStack() {
        final File dir = dir().newDir();
        screen().selectItem(dir).assertCurrentDirectory(dir)
                .selectTab(0).assertCurrentDirectory(dir().get());
    }
}
