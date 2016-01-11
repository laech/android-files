package l.files.ui.browser;

import l.files.fs.Files;
import l.files.fs.Path;

public final class BookmarkMenuTest extends BaseFilesActivityTest {

    public void test_bookmark_menu_is_unchecked_for_non_bookmarked_directory()
            throws Exception {

        Path dir1 = Files.createDir(dir().resolve("Not bookmarked 1"));
        Path dir2 = Files.createDir(dir().resolve("Not bookmarked 2"));
        screen()
                .clickInto(dir1)
                .assertBookmarkMenuChecked(false)
                .pressBack()
                .clickInto(dir2)
                .assertBookmarkMenuChecked(false);
    }

    public void test_bookmark_menu_is_checked_for_bookmarked_directory()
            throws Exception {

        Path dir = Files.createDir(dir().resolve("Bookmarked"));
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true);
    }

    public void test_bookmark_unbookmark_directory_checks_bookmark_menu_correctly()
            throws Exception {

        Path dir = Files.createDir(dir().resolve("Bookmarked then unbookmarked"));
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true)
                .unbookmark()
                .assertBookmarkMenuChecked(false);
    }

    public void test_navigate_through_bookmarked_unbookmarked_directories_checks_bookmark_menu_correctly()
            throws Exception {

        Path bookmarked = Files.createDir(dir().resolve("Bookmarked"));
        Path unbookmarked = Files.createDir(dir().resolve("Bookmarked/Unbookmarked"));
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
