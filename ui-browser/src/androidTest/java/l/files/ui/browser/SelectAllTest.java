package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;

@RunWith(AndroidJUnit4.class)
public final class SelectAllTest extends BaseFilesActivityTest {

    @Test
    public void selects_all() throws Exception {
        Path a = dir().concat("a").createFile();
        Path b = dir().concat("b").createFile();
        Path c = dir().concat("c").createDir();

        screen()
                .longClick(a)
                .selectAll()
                .assertChecked(a, true)
                .assertChecked(b, true)
                .assertChecked(c, true);
    }

    @Test
    public void finishes_action_mode_on_no_selection() throws Throwable {
        Path a = dir().concat("a").createFile();
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .click(a)
                .assertActionModePresent(false);
    }
}
