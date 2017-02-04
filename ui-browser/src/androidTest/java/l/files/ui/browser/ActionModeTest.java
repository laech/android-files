package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.testing.fs.Paths;

import static l.files.ui.browser.FileSort.NAME;

@RunWith(AndroidJUnit4.class)
public final class ActionModeTest extends BaseFilesActivityTest {

    @Test
    public void disabled_item_can_still_be_selected() throws Exception {
        Path a = dir().concat("a").createFile();
        Paths.removePermissions(a, Permission.read());
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .assertChecked(a, true);
    }

    @Test
    public void auto_finishes_action_mode_if_selected_item_deleted_from_file_system()
            throws Exception {

        Path a = dir().concat("a").createFile();
        screen().longClick(a).assertActionModeTitle(1);
        a.delete();
        screen().assertActionModePresent(false);
    }

    @Test
    public void title_shows_correct_selected_item_count_after_selected_item_deletion()
            throws Exception {

        Path a = dir().concat("a").createFile();
        Path b = dir().concat("b").createFile();
        Path c = dir().concat("c").createFile();

        screen()
                .longClick(a)
                .click(b)
                .assertActionModeTitle(2);

        a.delete();

        screen()
                .assertActionModePresent(true)
                .assertActionModeTitle(1)
                .click(c)
                .assertActionModeTitle(2);
    }

    @Test
    public void old_checked_item_remains_checked_when_new_item_added()
            throws Exception {

        Path a = dir().concat("a").createFile();

        screen()
                .sort()
                .by(NAME)
                .longClick(a)
                .assertChecked(a, true);

        Path b = dir().concat("b").createFile();

        screen()
                .assertChecked(b, false)
                .assertChecked(a, true);
    }

}
