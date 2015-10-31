package l.files.ui.browser;

import org.junit.Test;

import l.files.fs.File;

public final class BookmarksTest extends BaseFilesActivityTest {

    @Test
    public void clears_selection_on_finish_of_action_mode() throws Exception {

        File a = dir().resolve("a").createDir();
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

        File a = dir().resolve("a").createDir();
        File b = dir().resolve("b").createDir();
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

    @Test
    public void sidebar_displays_up_to_date_bookmarks() throws Exception {

        File a = dir().resolve("a").createDir();
        File b = dir().resolve("b").createDir();
        File c = dir().resolve("c").createDir();

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
