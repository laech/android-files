package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;

@RunWith(AndroidJUnit4.class)
public final class BookmarksTest extends BaseFilesActivityTest {

    @Test
    public void clears_selection_on_finish_of_action_mode() throws Exception {

        Path a = dir().concat("a").createDir();
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

        Path a = dir().concat("a").createDir();
        Path b = dir().concat("b").createDir();
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

        Path a = dir().concat("a").createDir();
        Path b = dir().concat("b").createDir();
        Path c = dir().concat("c").createDir();

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
