package l.files.features;

import l.files.fs.File;
import l.files.test.BaseFilesActivityTest;

public final class BookmarkMenuTest extends BaseFilesActivityTest
{

    public void test_bookmark_menu_is_unchecked_for_non_bookmarked_directory()
            throws Exception
    {
        final File dir1 = dir().resolve("Not bookmarked 1").createDirectory();
        final File dir2 = dir().resolve("Not bookmarked 2").createDirectory();
        screen()
                .clickInto(dir1)
                .assertBookmarkMenuChecked(false)
                .pressBack()
                .clickInto(dir2)
                .assertBookmarkMenuChecked(false);
    }

    public void test_bookmark_menu_is_checked_for_bookmarked_directory()
            throws Exception
    {
        final File dir = dir().resolve("Bookmarked").createDirectory();
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true);
    }

    public void test_bookmark_unbookmark_directory_checks_bookmark_menu_correctly()
            throws Exception
    {
        final File dir = dir().resolve("Bookmarked then unbookmarked").createDirectory();
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true)
                .unbookmark()
                .assertBookmarkMenuChecked(false);
    }

    public void test_navigate_through_bookmarked_unbookmarked_directories_checks_bookmark_menu_correctly()
            throws Exception
    {
        final File bookmarked = dir().resolve("Bookmarked").createDirectory();
        final File unbookmarked = dir().resolve("Bookmarked/Unbookmarked").createDirectory();
        screen()
                .clickInto(bookmarked)
                .bookmark()
                .assertBookmarkMenuChecked(true)
                .clickInto(unbookmarked)
                .assertBookmarkMenuChecked(false)
                .pressBack()
                .assertBookmarkMenuChecked(true)
                .clickInto(unbookmarked)
                .assertBookmarkMenuChecked(false);
    }

}
