package l.files.ui.browser;

import l.files.fs.File;
import l.files.fs.Stream;

import static l.files.fs.LinkOption.NOFOLLOW;

public final class ActionModeTest extends BaseFilesActivityTest {

    public void test_can_start_action_mode_after_rotation() throws Exception {
        for (int i = 0; i < 10; i++) {
            dir().resolve(String.valueOf(i)).createFile();
        }

        try (Stream<File> stream = dir().list(NOFOLLOW)) {
            File child = stream.iterator().next();
            screen()
                    .rotate()
                    .longClick(child)
                    .assertChecked(child, true)
                    .assertActionModePresent(true);
        }
    }

    public void test_clears_selection_on_finish_of_action_mode() throws Exception {
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

    public void test_maintains_action_mode_on_screen_rotation() throws Exception {
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
