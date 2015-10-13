package l.files.features;

import l.files.fs.File;
import l.files.testing.BaseFilesActivityTest;

public final class BookmarkMenuTest extends BaseFilesActivityTest {

    public void test_bookmark_menu_is_unchecked_for_non_bookmarked_directory()
            throws Exception {
        final File dir1 = dir().resolve("Not bookmarked 1").createDir();
        final File dir2 = dir().resolve("Not bookmarked 2").createDir();
        screen()
                .clickInto(dir1)
                .assertBookmarkMenuChecked(false)
                .pressBack()
                .clickInto(dir2)
                .assertBookmarkMenuChecked(false);
    }

    public void test_bookmark_menu_is_checked_for_bookmarked_directory()
            throws Exception {
        final File dir = dir().resolve("Bookmarked").createDir();
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true);
    }

    public void test_bookmark_unbookmark_directory_checks_bookmark_menu_correctly()
            throws Exception {
        final File dir = dir().resolve("Bookmarked then unbookmarked").createDir();
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true)
                .unbookmark()
                .assertBookmarkMenuChecked(false);
    }

    public void test_navigate_through_bookmarked_unbookmarked_directories_checks_bookmark_menu_correctly()
            throws Exception {
        final File bookmarked = dir().resolve("Bookmarked").createDir();
        final File unbookmarked = dir().resolve("Bookmarked/Unbookmarked").createDir();
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
