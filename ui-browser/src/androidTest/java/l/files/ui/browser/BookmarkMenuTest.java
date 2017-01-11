package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;

@RunWith(AndroidJUnit4.class)
public final class BookmarkMenuTest extends BaseFilesActivityTest {

    @Test
    public void bookmark_menu_is_unchecked_for_non_bookmarked_directory()
            throws Exception {

        Path dir1 = fs.createDir(dir().concat("Not bookmarked 1"));
        Path dir2 = fs.createDir(dir().concat("Not bookmarked 2"));
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

        Path dir = fs.createDir(dir().concat("Bookmarked"));
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true);
    }

    @Test
    public void bookmark_unbookmark_directory_checks_bookmark_menu_correctly()
            throws Exception {

        Path dir = fs.createDir(dir().concat("Bookmarked then unbookmarked"));
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

        Path bookmarked = fs.createDir(dir().concat("Bookmarked"));
        Path unbookmarked = fs.createDir(dir().concat("Bookmarked/Unbookmarked"));
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
