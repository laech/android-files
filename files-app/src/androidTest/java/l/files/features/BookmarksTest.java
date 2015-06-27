package l.files.features;

import l.files.fs.Resource;
import l.files.test.BaseFilesActivityTest;

public final class BookmarksTest extends BaseFilesActivityTest
{

    public void test_clears_selection_on_finish_of_action_mode() throws Exception
    {
        final Resource a = dir().resolve("a").createDirectory();
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

    public void test_maintains_action_mode_on_screen_rotation() throws Exception
    {
        final Resource a = dir().resolve("a").createDirectory();
        final Resource b = dir().resolve("b").createDirectory();
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

    public void test_click_on_bookmark_opens_directory() throws Exception
    {
        final Resource a = dir().resolve("a").createDirectory();
        final Resource b = dir().resolve("b").createDirectory();
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

    public void test_bookmarks_sidebar_locked_on_bookmarks_action_mode() throws Exception
    {
        final Resource a = dir().resolve("a").createDirectory();

        screen()
                .clickInto(a)
                .bookmark()
                .openBookmarksDrawer()
                .longClick(a)
                .activityObject()
                .assertBookmarksSidebarIsOpenLocked(true);
    }

    public void test_delete_bookmarks_from_sidebar() throws Exception
    {
        final Resource a = dir().resolve("a").createDirectory();
        final Resource b = dir().resolve("b").createDirectory();
        final Resource c = dir().resolve("c").createDirectory();

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

    public void test_bookmark_appears_in_sidebar() throws Exception
    {
        screen()
                .clickInto(dir().resolve("a").createDirectory())
                .bookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(true)
                .activityObject()
                .unbookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(false);
    }

    public void test_bookmarks_are_sorted_by_name() throws Exception
    {
        final Resource b = dir().resolve("b").createDirectory();
        final Resource a = dir().resolve("a").createDirectory();
        final Resource c = dir().resolve("c").createDirectory();
        screen()
                .clickInto(a).bookmark().pressBack()
                .clickInto(c).bookmark().pressBack()
                .clickInto(b).bookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(true)
                .assertContainsBookmarksInOrder(a, b, c);
    }

}
