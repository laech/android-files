package l.files.ui.browser;

import org.junit.Test;

import java.nio.file.Path;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;

public final class SelectAllTest extends BaseFilesActivityTest {

    @Test
    public void selects_all() throws Exception {
        Path a = createFile(dir().resolve("a"));
        Path b = createFile(dir().resolve("b"));
        Path c = createDirectory(dir().resolve("c"));

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
