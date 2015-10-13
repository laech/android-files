package l.files.features;

import l.files.fs.File;
import l.files.testing.BaseFilesActivityTest;

public final class BookmarksTest extends BaseFilesActivityTest {

    public void test_clears_selection_on_finish_of_action_mode() throws Exception {
        final File a = dir().resolve("a").createDir();
        screen()
                .clickInto(a)
                .bookmark()
                .pressBack()

                .openBookmarksDrawer()
                .longClick(a)
                .assertActionModePresent(true)
                .assertDrawerIsOpened(true)
                .assertChecked(a, true)

                .pressBack()
                .assertActionModePresent(false)
                .assertDrawerIsOpened(true)
                .assertChecked(a, false)

                .rotate()
                .assertActionModePresent(false)
                .assertDrawerIsOpened(true)
                .assertChecked(a, false);
    }

    public void test_maintains_action_mode_on_screen_rotation() throws Exception {
        final File a = dir().resolve("a").createDir();
        final File b = dir().resolve("b").createDir();
        screen()
                .clickInto(a).bookmark().pressBack()
                .clickInto(b).bookmark().pressBack()
                .openBookmarksDrawer()
                .longClick(a)
                .assertActionModePresent(true)
                .assertActionModeTitle(1)

                .rotate()
                .assertDrawerIsOpened(true)
                .assertActionModePresent(true)
                .assertActionModeTitle(1)
                .assertChecked(a, true)
                .assertChecked(b, false)

                .click(b)
                .assertActionModePresent(true)
                .assertActionModeTitle(2);
    }

    public void test_click_on_bookmark_opens_directory() throws Exception {
        final File a = dir().resolve("a").createDir();
        final File b = dir().resolve("b").createDir();
        screen()
                .clickInto(a)
                .assertCurrentDirectory(a)
                .bookmark()
                .pressBack()

                .clickInto(b)
                .assertCurrentDirectory(b)
                .bookmark()

                .openBookmarksDrawer()
                .click(a)
                .activityObject()
                .assertCurrentDirectory(b)
                .assertBookmarksSidebarIsClosed();

    }

    public void test_bookmarks_sidebar_locked_on_bookmarks_action_mode() throws Exception {
        final File a = dir().resolve("a").createDir();

        screen()
                .clickInto(a)
                .bookmark()
                .openBookmarksDrawer()
                .longClick(a)
                .activityObject()
                .assertBookmarksSidebarIsOpenLocked(true);
    }

    public void test_delete_bookmarks_from_sidebar() throws Exception {
        final File a = dir().resolve("a").createDir();
        final File b = dir().resolve("b").createDir();
        final File c = dir().resolve("c").createDir();

        screen()

                .clickInto(a).bookmark().pressBack()
                .clickInto(b).bookmark().pressBack()
                .clickInto(c).bookmark().pressBack()

                .openBookmarksDrawer()
                .assertBookmarked(a, true)
                .assertBookmarked(b, true)
                .assertBookmarked(c, true)

                .longClick(a)
                .click(b)
                .delete()

                .assertBookmarked(a, false)
                .assertBookmarked(b, false)
                .assertBookmarked(c, true);
    }

    public void test_bookmark_appears_in_sidebar() throws Exception {
        screen()
                .clickInto(dir().resolve("a").createDir())
                .bookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(true)
                .activityObject()
                .unbookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(false);
    }

    public void test_bookmarks_are_sorted_by_name() throws Exception {
        final File b = dir().resolve("b").createDir();
        final File a = dir().resolve("a").createDir();
        final File c = dir().resolve("c").createDir();
        screen()
                .clickInto(a).bookmark().pressBack()
                .clickInto(c).bookmark().pressBack()
                .clickInto(b).bookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(true)
                .assertContainsBookmarksInOrder(a, b, c);
    }

}
