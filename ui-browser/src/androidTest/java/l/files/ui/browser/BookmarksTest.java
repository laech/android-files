package l.files.ui.browser;

import l.files.fs.Files;
import l.files.fs.Path;

public final class BookmarksTest extends BaseFilesActivityTest {

    public void test_clears_selection_on_finish_of_action_mode() throws Exception {

        Path a = Files.createDir(dir().resolve("a"));
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

    public void test_click_on_bookmark_opens_directory() throws Exception {

        Path a = Files.createDir(dir().resolve("a"));
        Path b = Files.createDir(dir().resolve("b"));
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
                .assertCurrentDirectory(b)
                .assertBookmarksSidebarIsClosed();

    }

    public void test_sidebar_displays_up_to_date_bookmarks() throws Exception {

        Path a = Files.createDir(dir().resolve("a"));
        Path b = Files.createDir(dir().resolve("b"));
        Path c = Files.createDir(dir().resolve("c"));

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
