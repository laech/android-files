package l.files.ui.browser;

import l.files.testing.fs.Paths;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static l.files.ui.browser.sort.FileSort.NAME;

public final class ActionModeTest extends BaseFilesActivityTest {

    @Test
    public void disabled_item_can_still_be_selected() throws Exception {
        Path a = createFile(dir().resolve("a"));
        Paths.removePermissions(
            l.files.fs.Path.of(a),
            PosixFilePermissions.fromString("r--r--r--")
        );
        screen()
            .longClick(a)
            .assertActionModePresent(true)
            .assertChecked(a, true);
    }

    @Test
    public void auto_finishes_action_mode_if_selected_item_deleted_from_file_system()
        throws Exception {

        Path a = createFile(dir().resolve("a"));
        screen().longClick(a).assertActionModeTitle(1);
        delete(a);
        screen().assertActionModePresent(false);
    }

    @Test
    public void title_shows_correct_selected_item_count_after_selected_item_deletion()
        throws Exception {

        Path a = createFile(dir().resolve("a"));
        Path b = createFile(dir().resolve("b"));
        Path c = createFile(dir().resolve("c"));

        screen()
            .longClick(a)
            .click(b)
            .assertActionModeTitle(2);

        delete(a);

        screen()
            .assertActionModePresent(true)
            .assertActionModeTitle(1)
            .click(c)
            .assertActionModeTitle(2);
    }

    @Test
    public void old_checked_item_remains_checked_when_new_item_added()
        throws Exception {

        Path a = createFile(dir().resolve("a"));

        screen()
            .sort()
            .by(NAME)
            .longClick(a)
            .assertChecked(a, true);

        Path b = createFile(dir().resolve("b"));

        screen()
            .assertChecked(b, false)
            .assertChecked(a, true);
    }

}
