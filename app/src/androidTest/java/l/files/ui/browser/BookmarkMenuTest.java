package l.files.ui.browser;

import org.junit.Test;

import l.files.fs.File;

public final class BookmarkMenuTest extends BaseFilesActivityTest {

    @Test
    public void bookmark_menu_is_unchecked_for_non_bookmarked_directory()
            throws Exception {

        File dir1 = dir().resolve("Not bookmarked 1").createDir();
        File dir2 = dir().resolve("Not bookmarked 2").createDir();
        screen()
                .clickInto(dir1)
                .assertBookmarkMenuChecked(false)
                .pressBack()
                .clickInto(dir2)
                .assertBookmarkMenuChecked(false);
    }

    @Test
    public void bookmark_menu_is_checked_for_bookmarked_directory()
            throws Exception {

        File dir = dir().resolve("Bookmarked").createDir();
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true);
    }

    @Test
    public void bookmark_unbookmark_directory_checks_bookmark_menu_correctly()
            throws Exception {

        File dir = dir().resolve("Bookmarked then unbookmarked").createDir();
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true)
                .unbookmark()
                .assertBookmarkMenuChecked(false);
    }

    @Test
    public void navigate_through_bookmarked_unbookmarked_directories_checks_bookmark_menu_correctly()
            throws Exception {

        File bookmarked = dir().resolve("Bookmarked").createDir();
        File unbookmarked = dir().resolve("Bookmarked/Unbookmarked").createDir();
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
