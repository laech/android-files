package l.files.features;

import l.files.fs.Resource;
import l.files.test.BaseFilesActivityTest;

public final class BookmarksTest extends BaseFilesActivityTest
{
    public void test_click_on_bookmark_opens_directory() throws Exception
    {
        final Resource a = directory().resolve("a").createDirectory();
        final Resource b = directory().resolve("b").createDirectory();
        screen()
                .click(a)
                .assertCurrentDirectory(a)
                .bookmark()
                .pressBack()

                .click(b)
                .assertCurrentDirectory(b)
                .bookmark()

                .openBookmarksDrawer()
                .click(a)
                .assertCurrentDirectory(b)
                .assertBookmarksSidebarIsClosed();

    }

    public void test_bookmarks_sidebar_locked_on_bookmarks_action_mode() throws Exception
    {
        final Resource a = directory().resolve("a").createDirectory();

        screen()
                .click(a)
                .bookmark()
                .openBookmarksDrawer()
                .toggleSelection(a)
                .activityObject()
                .assertBookmarksSidebarIsOpenLocked(true);
    }

    public void test_delete_bookmarks_from_sidebar() throws Exception
    {
        final Resource a = directory().resolve("a").createDirectory();
        final Resource b = directory().resolve("b").createDirectory();
        final Resource c = directory().resolve("c").createDirectory();

        screen()

                .click(a).bookmark().pressBack()
                .click(b).bookmark().pressBack()
                .click(c).bookmark().pressBack()

                .openBookmarksDrawer()
                .assertBookmarked(a, true)
                .assertBookmarked(b, true)
                .assertBookmarked(c, true)

                .toggleSelection(a)
                .toggleSelection(b)
                .delete()

                .assertBookmarked(a, false)
                .assertBookmarked(b, false)
                .assertBookmarked(c, true);
    }

    public void test_bookmark_appears_in_sidebar() throws Exception
    {
        screen()
                .click(directory().resolve("a").createDirectory())
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
        final Resource b = directory().resolve("b").createDirectory();
        final Resource a = directory().resolve("a").createDirectory();
        final Resource c = directory().resolve("c").createDirectory();
        screen()
                .click(a).bookmark().pressBack()
                .click(c).bookmark().pressBack()
                .click(b).bookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(true)
                .assertContainsBookmarksInOrder(a, b, c);
    }

}
