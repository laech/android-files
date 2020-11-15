package l.files.ui.browser;

import org.junit.Test;

import java.nio.file.Path;

import static java.nio.file.Files.createDirectory;

public final class BookmarkMenuTest extends BaseFilesActivityTest {

    @Test
    public void bookmark_menu_is_unchecked_for_non_bookmarked_directory()
        throws Exception {

        Path dir1 = createDirectory(dir().resolve("Not bookmarked 1"));
        Path dir2 = createDirectory(dir().resolve("Not bookmarked 2"));
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

        Path dir = createDirectory(dir().resolve("Bookmarked"));
        screen()
            .clickInto(dir)
            .bookmark()
            .assertBookmarkMenuChecked(true);
    }

    @Test
    public void bookmark_unbookmark_directory_checks_bookmark_menu_correctly()
        throws Exception {

        Path dir =
            createDirectory(dir().resolve("Bookmarked then unbookmarked"));
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

        Path bookmarked = createDirectory(dir().resolve("Bookmarked"));
        Path unbookmarked =
            createDirectory(dir().resolve("Bookmarked/Unbookmarked"));
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
