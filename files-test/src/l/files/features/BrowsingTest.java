package l.files.features;

import l.files.test.BaseFilesActivityTest;

import java.io.File;

public final class BrowsingTest extends BaseFilesActivityTest {

    public void testOpenNewDirectoryWillCloseOpenedDrawer() {
        final File dir = dir().newDir();
        screen().openDrawer()
                .selectItem(dir)
                .assertDrawerIsOpened(false);
    }
}
