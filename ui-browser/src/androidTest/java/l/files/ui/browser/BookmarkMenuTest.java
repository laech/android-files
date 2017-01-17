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

        Path dir1 = dir().concat("Not bookmarked 1").createDir();
        Path dir2 = dir().concat("Not bookmarked 2").createDir();
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

        Path dir = dir().concat("Bookmarked").createDir();
        screen()
                .clickInto(dir)
                .bookmark()
                .assertBookmarkMenuChecked(true);
    }

    @Test
    public void bookmark_unbookmark_directory_checks_bookmark_menu_correctly()
            throws Exception {

        Path dir = dir().concat("Bookmarked then unbookmarked").createDir();
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

        Path bookmarked = dir().concat("Bookmarked").createDir();
        Path unbookmarked = dir().concat("Bookmarked/Unbookmarked").createDir();
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
