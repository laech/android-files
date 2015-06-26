package l.files.features;

import l.files.fs.Resource;
import l.files.test.BaseFilesActivityTest;

public final class BookmarkMenuTest extends BaseFilesActivityTest
{

    public void test_bookmark_menu_is_unchecked_for_non_bookmarked_directory()
            throws Exception
    {
        final Resource dir1 = directory().resolve("Not bookmarked 1").createDirectory();
        final Resource dir2 = directory().resolve("Not bookmarked 2").createDirectory();
        screen()
                .click(dir1)
                .assertBookmarkMenuChecked(false)
                .pressBack()
                .click(dir2)
                .assertBookmarkMenuChecked(false);
    }

    public void test_bookmark_menu_is_checked_for_bookmarked_directory()
            throws Exception
    {
        final Resource dir = directory().resolve("Bookmarked").createDirectory();
        screen()
                .click(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true);
    }

    public void test_bookmark_unbookmark_directory_checks_bookmark_menu_correctly()
            throws Exception
    {
        final Resource dir = directory().resolve("Bookmarked then unbookmarked").createDirectory();
        screen()
                .click(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true)
                .unbookmark()
                .assertBookmarkMenuChecked(false);
    }

    public void test_navigate_through_bookmarked_unbookmarked_directories_checks_bookmark_menu_correctly()
            throws Exception
    {
        final Resource bookmarked = directory().resolve("Bookmarked").createDirectory();
        final Resource unbookmarked = directory().resolve("Bookmarked/Unbookmarked").createDirectory();
        screen()
                .click(bookmarked)
                .bookmark()
                .assertBookmarkMenuChecked(true)
                .click(unbookmarked)
                .assertBookmarkMenuChecked(false)
                .pressBack()
                .assertBookmarkMenuChecked(true)
                .click(unbookmarked)
                .assertBookmarkMenuChecked(false);
    }

}
