package l.files.features;

import l.files.fs.File;
import l.files.test.BaseFilesActivityTest;

public final class SelectAllTest extends BaseFilesActivityTest {

    public void test_selects_all() throws Exception {
        final File a = dir().resolve("a").createFile();
        final File b = dir().resolve("b").createFile();
        final File c = dir().resolve("c").createDirectory();

        screen()
                .longClick(a)
                .selectAll()
                .assertChecked(a, true)
                .assertChecked(b, true)
                .assertChecked(c, true);
    }

    public void test_finishes_action_mode_on_no_selection() throws Throwable {
        final File a = dir().resolve("a").createFile();
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .click(a)
                .assertActionModePresent(false);
    }
}
