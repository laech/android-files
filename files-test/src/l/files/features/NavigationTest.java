package l.files.features;

import l.files.test.BaseFilesActivityTest;

import java.io.File;

public final class NavigationTest extends BaseFilesActivityTest {

    public void testOpenNewDirectoryWillCloseOpenedDrawer() {
        final File dir = dir().newDir();
        screen().openDrawer()
                .selectItem(dir)
                .assertDrawerIsOpened(false);
    }

    public void testObservesOnCurrentDirectoryAndShowsNewlyAddedFiles() {
        screen().assertListViewContains(dir().newDir(), true);
    }

    public void testObservesOnCurrentDirectoryAndHidesDeletedFiles() {
        final File file = dir().newFile();
        screen().assertListViewContains(file, true)
                .assertListViewContains(delete(file), false);
    }

    private File delete(File file) {
        assertTrue(file.delete());
        return file;
    }
}
