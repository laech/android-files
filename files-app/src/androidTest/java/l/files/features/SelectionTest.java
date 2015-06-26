package l.files.features;

import l.files.fs.Resource;
import l.files.test.BaseFilesActivityTest;

public final class SelectionTest extends BaseFilesActivityTest
{

    public void test_selects_all() throws Exception
    {
        final Resource a = directory().resolve("a").createFile();
        final Resource b = directory().resolve("b").createFile();
        final Resource c = directory().resolve("c").createDirectory();

        screen()
                .toggleSelection(a)
                .selectAll()
                .assertChecked(a, true)
                .assertChecked(b, true)
                .assertChecked(c, true);
    }

    public void test_finishes_action_mode_on_no_selection() throws Throwable
    {
        final Resource a = directory().resolve("a").createFile();
        screen()
                .toggleSelection(a)
                .assertActionModePresent(true)
                .toggleSelection(a)
                .assertActionModePresent(false);
    }
}
