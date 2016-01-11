package l.files.ui.browser;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Permission;

import static l.files.ui.browser.FileSort.NAME;

public final class ActionModeTest extends BaseFilesActivityTest {

    public void test_disabled_item_can_still_be_selected() throws Exception {
        Path a = Files.createFile(dir().resolve("a"));
        Files.removePermissions(a, Permission.read());
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .assertChecked(a, true);
    }

    public void test_auto_finishes_action_mode_if_selected_item_deleted_from_file_system()
            throws Exception {

        Path a = Files.createFile(dir().resolve("a"));
        screen().longClick(a).assertActionModeTitle(1);
        Files.delete(a);
        screen().assertActionModePresent(false);
    }

    public void test_title_shows_correct_selected_item_count_after_selected_item_deletion()
            throws Exception {

        Path a = Files.createFile(dir().resolve("a"));
        Path b = Files.createFile(dir().resolve("b"));
        Path c = Files.createFile(dir().resolve("c"));

        screen()
                .longClick(a)
                .click(b)
                .assertActionModeTitle(2);

        Files.delete(a);

        screen()
                .assertActionModePresent(true)
                .assertActionModeTitle(1)
                .click(c)
                .assertActionModeTitle(2);
    }

    public void test_old_checked_item_remains_checked_when_new_item_added()
            throws Exception {

        Path a = Files.createFile(dir().resolve("a"));

        screen()
                .sort()
                .by(NAME)
                .longClick(a)
                .assertChecked(a, true);

        Path b = Files.createFile(dir().resolve("b"));

        screen()
                .assertChecked(b, false)
                .assertChecked(a, true);
    }

}
