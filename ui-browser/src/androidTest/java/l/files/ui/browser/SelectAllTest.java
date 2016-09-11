package l.files.ui.browser;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import l.files.fs.Path;

import static l.files.fs.Files.createDir;
import static l.files.fs.Files.createFile;

@RunWith(AndroidJUnit4.class)
public final class SelectAllTest extends BaseFilesActivityTest {

    @Test
    public void selects_all() throws Exception {
        Path a = createFile(dir().resolve("a"));
        Path b = createFile(dir().resolve("b"));
        Path c = createDir(dir().resolve("c"));

        screen()
                .longClick(a)
                .selectAll()
                .assertChecked(a, true)
                .assertChecked(b, true)
                .assertChecked(c, true);
    }

    @Test
    public void finishes_action_mode_on_no_selection() throws Throwable {
        Path a = createFile(dir().resolve("a"));
        screen()
                .longClick(a)
                .assertActionModePresent(true)
                .click(a)
                .assertActionModePresent(false);
    }
}
