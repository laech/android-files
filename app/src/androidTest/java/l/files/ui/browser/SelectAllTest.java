package l.files.ui.browser;

import org.junit.Test;

import l.files.fs.File;

public final class SelectAllTest extends BaseFilesActivityTest {

    @Test
    public void selects_all() throws Exception {
        final File a = dir().resolve("a").createFile();
        final File b = dir().resolve("b").createFile();
        final File c = dir().resolve("c").createDir();

        screen()
                .longClick(a)
                .selectAll()
                .assertChecked(a, true)
                .assertChecked(b, true)
                .assertChecked(c, true);
    }

    @Test
    public void finishes_action_mode_on_no_selection() throws Throwable {
        final File a = dir().resolve("a").createFile();
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .click(a)
                .assertActionModePresent(false);
    }
}
