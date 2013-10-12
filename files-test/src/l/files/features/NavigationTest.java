package l.files.features;

import l.files.test.BaseFilesActivityTest;

import java.io.File;
import java.io.IOException;

import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;

public final class NavigationTest extends BaseFilesActivityTest {

    public void testActionBarTitleShowsNameOfDirectory() {
        screen().selectItem(dir().newDir("a"))
                .assertActionBarTitle("a");
    }

    public void testActionBarHidesUpIndicatorWhenThereIsNoBackStackInitially() {
        screen().assertActionBarUpIndicatorIsVisible(false);
    }

    public void testActionBarShowsUpIndicatorWhenThereIsBackStack() {
        screen().selectItem(dir().newDir())
                .assertActionBarUpIndicatorIsVisible(true);
    }

    public void testActionBarHidesUpIndicatorWhenThereIsNoBackStackToGoBackTo() {
        screen().selectItem(dir().newDir())
                .pressBack()
                .assertActionBarUpIndicatorIsVisible(false);
    }

    public void testLongPressBackWillClearBackStack() {
        screen().selectItem(dir().newDir("a"))
                .selectItem(dir().newDir("a/b"))
                .selectItem(dir().newDir("a/b/c"))
                .longPressBack()
                .assertCurrentDirectory(dir().get());
    }

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

    public void testUpdatesViewOnChildDirectoryModified() throws Exception {
        testUpdatesViewOnChildModified(dir().newDir());
    }

    public void testUpdatesViewOnChildFileModified() throws Exception {
        testUpdatesViewOnChildModified(dir().newFile());
    }

    private void testUpdatesViewOnChildModified(File f) throws IOException {
        assertTrue(f.setLastModified(0));
        screen().assertFileSummaryIsUpToDate(f)
                .assertFileSummaryIsUpToDate(modify(f));
    }

    private File delete(File file) {
        assertTrue(file.delete());
        return file;
    }

    private File modify(File file) throws IOException {
        final long lastModifiedBefore = file.lastModified();
        if (file.isDirectory()) {
            assertTrue(new File(file, String.valueOf(System.nanoTime())).mkdir());
        } else {
            write("test", file, UTF_8);
        }
        final long lastModifiedAfter = file.lastModified();
        assertNotEqual(lastModifiedBefore, lastModifiedAfter);
        return file;
    }
}
