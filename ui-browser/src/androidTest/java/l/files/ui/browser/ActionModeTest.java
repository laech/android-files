package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.testing.fs.Files;

import static l.files.ui.browser.FileSort.NAME;

@RunWith(AndroidJUnit4.class)
public final class ActionModeTest extends BaseFilesActivityTest {

    @Test
    public void disabled_item_can_still_be_selected() throws Exception {
        Path a = fs.createFile(dir().concat("a"));
        Files.removePermissions(fs, a, Permission.read());
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .assertChecked(a, true);
    }

    @Test
    public void auto_finishes_action_mode_if_selected_item_deleted_from_file_system()
            throws Exception {

        Path a = fs.createFile(dir().concat("a"));
        screen().longClick(a).assertActionModeTitle(1);
        fs.delete(a);
        screen().assertActionModePresent(false);
    }

    @Test
    public void title_shows_correct_selected_item_count_after_selected_item_deletion()
            throws Exception {

        Path a = fs.createFile(dir().concat("a"));
        Path b = fs.createFile(dir().concat("b"));
        Path c = fs.createFile(dir().concat("c"));

        screen()
                .longClick(a)
                .click(b)
                .assertActionModeTitle(2);

        fs.delete(a);

        screen()
                .assertActionModePresent(true)
                .assertActionModeTitle(1)
                .click(c)
                .assertActionModeTitle(2);
    }

    @Test
    public void old_checked_item_remains_checked_when_new_item_added()
            throws Exception {

        Path a = fs.createFile(dir().concat("a"));

        screen()
                .sort()
                .by(NAME)
                .longClick(a)
                .assertChecked(a, true);

        Path b = fs.createFile(dir().concat("b"));

        screen()
                .assertChecked(b, false)
                .assertChecked(a, true);
    }

}
