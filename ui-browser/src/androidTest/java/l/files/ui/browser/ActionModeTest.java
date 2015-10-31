package l.files.ui.browser;

import org.junit.Test;

import l.files.fs.File;
import l.files.fs.Permission;

import static l.files.ui.browser.FileSort.NAME;

public final class ActionModeTest extends BaseFilesActivityTest {

    @Test
    public void disabled_item_can_still_be_selected() throws Exception {
        File a = dir().resolve("a").createFile();
        a.removePermissions(Permission.read());
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .assertChecked(a, true);
    }

    @Test
    public void auto_finishes_action_mode_if_selected_item_deleted_from_file_system()
            throws Exception {

        File a = dir().resolve("a").createFile();
        screen().longClick(a).assertActionModeTitle(1);
        a.delete();
        screen().assertActionModePresent(false);
    }

    @Test
    public void title_shows_correct_selected_item_count_after_selected_item_deletion()
            throws Exception {

        File a = dir().resolve("a").createFile();
        File b = dir().resolve("b").createFile();
        File c = dir().resolve("c").createFile();

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

        File a = dir().resolve("a").createFile();

        screen()
                .sort()
                .by(NAME)
                .longClick(a)
                .assertChecked(a, true);

        File b = dir().resolve("b").createFile();

        screen()
                .assertChecked(b, false)
                .assertChecked(a, true);
    }

}
