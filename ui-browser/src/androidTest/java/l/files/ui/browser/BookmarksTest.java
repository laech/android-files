package l.files.ui.browser;

import org.junit.Test;

import java.nio.file.Path;

import static java.nio.file.Files.createDirectory;

public final class BookmarksTest extends BaseFilesActivityTest {

    @Test
    public void clears_selection_on_finish_of_action_mode() throws Exception {

        Path a = createDirectory(dir().resolve("a"));
        screen()
            .clickInto(a)
            .bookmark()
            .pressBack()

            .openBookmarksDrawer()
            .longClick(a)
            .assertActionModePresent(true)
            .assertDrawerIsOpened(true)
            .assertChecked(a, true)
            .assertActionModeTitle(1)

            .pressBack()
            .assertActionModePresent(false)
            .assertDrawerIsOpened(true)
            .assertChecked(a, false);
    }

    @Test
    public void click_on_bookmark_opens_directory() throws Exception {

        Path a = createDirectory(dir().resolve("a"));
        Path b = createDirectory(dir().resolve("b"));
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
            .assertCurrentDirectory(a)
            .assertBookmarksSidebarIsClosed();

    }

    @Test
    public void sidebar_displays_up_to_date_bookmarks() throws Exception {

        Path a = createDirectory(dir().resolve("a"));
        Path b = createDirectory(dir().resolve("b"));
        Path c = createDirectory(dir().resolve("c"));

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

}
