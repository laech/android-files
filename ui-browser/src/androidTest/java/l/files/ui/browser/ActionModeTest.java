package l.files.ui.browser;

import org.junit.Test;

import l.files.base.io.Closer;
import l.files.fs.File;
import l.files.fs.Permission;
import l.files.fs.Stream;

import static l.files.fs.LinkOption.NOFOLLOW;
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

    @Test
    public void can_start_action_mode_after_rotation() throws Exception {
        for (int i = 0; i < 10; i++) {
            dir().resolve(String.valueOf(i)).createFile();
        }

        Closer closer = Closer.create();
        try {

            Stream<File> stream = closer.register(dir().list(NOFOLLOW));
            File child = stream.iterator().next();
            screen()
                    .rotate()
                    .longClick(child)
                    .assertChecked(child, true)
                    .assertActionModePresent(true);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    @Test
    public void clears_selection_on_finish_of_action_mode() throws Exception {
        File a = dir().resolve("a").createFile();
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .assertChecked(a, true)
                .assertActionModeTitle(1)

                .pressBack()
                .assertActionModePresent(false)
                .assertChecked(a, false)

                .rotate()
                .assertActionModePresent(false)
                .assertChecked(a, false);
    }

    @Test
    public void maintains_action_mode_on_screen_rotation() throws Exception {
        File a = dir().resolve("a").createFile();
        File b = dir().resolve("b").createFile();
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .assertActionModeTitle(1)

                .rotate()
                .assertActionModePresent(true)
                .assertActionModeTitle(1)
                .assertChecked(a, true)
                .assertChecked(b, false)

                .click(b)
                .assertActionModePresent(true)
                .assertActionModeTitle(2);
    }

}
