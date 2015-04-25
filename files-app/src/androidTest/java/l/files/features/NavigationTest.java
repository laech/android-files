package l.files.features;

import java.io.File;
import java.io.IOException;

import l.files.common.base.Consumer;
import l.files.fs.Resource;
import l.files.fs.local.LocalResource;
import l.files.test.BaseFilesActivityTest;

import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Files.append;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class NavigationTest extends BaseFilesActivityTest {

    // TODO test symbolic link to directory

    public void testSymbolicLinkIconDisplayed() throws Exception {
        Resource dir = resource().resolve("dir");
        Resource link = resource().resolve("link");
        dir.createDirectory();
        link.createSymbolicLink(dir);

        screen()
                .assertSymbolicLinkIconDisplayed(dir, false)
                .assertSymbolicLinkIconDisplayed(link, true);
    }

    public void testCanNavigateIntoSymlinkDirectory() throws Exception {
        Resource dir = resource().resolve("dir");
        dir.createDirectory();
        dir.resolve("a").createDirectory();

        Resource link = resource().resolve("link");
        link.createSymbolicLink(dir);
        Resource linkChild = link.resolve("a");

        screen()
                .selectItem(link)
                .selectItem(linkChild)
                .assertCurrentDirectory(linkChild);
    }

    public void testPressActionBarUpIndicatorWillGoBack() {
        File dir = dir().createDir("a");
        screen()
                .selectItem(dir)
                .assertCurrentDirectory(dir)
                .pressActionBarUpIndicator()
                .assertCurrentDirectory(dir.getParentFile());
    }

    public void testActionBarTitleShowsNameOfDirectory() {
        screen()
                .selectItem(dir().createDir("a"))
                .assertActionBarTitle("a");
    }

    public void testActionBarHidesUpIndicatorWhenThereIsNoBackStackInitially() {
        screen().assertActionBarUpIndicatorIsVisible(false);
    }

    public void testActionBarShowsUpIndicatorWhenThereIsBackStack() {
        screen()
                .selectItem(dir().createDir("a"))
                .assertActionBarUpIndicatorIsVisible(true);
    }

    public void testActionBarHidesUpIndicatorWhenThereIsNoBackStackToGoBackTo() {
        screen()
                .selectItem(dir().createDir("a"))
                .pressBack()
                .assertActionBarUpIndicatorIsVisible(false);
    }

    public void testLongPressBackWillClearBackStack() {
        screen()
                .selectItem(dir().createDir("a"))
                .selectItem(dir().createDir("a/b"))
                .selectItem(dir().createDir("a/b/c"))
                .longPressBack()
                .assertCurrentDirectory(dir().get());
    }

    public void testOpenNewDirectoryWillCloseOpenedDrawer() {
        Resource dir = LocalResource.create(dir().createDir("a"));
        screen()
                .openBookmarksDrawer()
                .getActivityObject()
                .selectItem(dir)
                .assertDrawerIsOpened(false);
    }

    public void testObservesOnCurrentDirectoryAndShowsNewlyAddedFiles() {
        screen().assertListViewContains(dir().createDir("a"), true);
    }

    public void testObservesOnCurrentDirectoryAndHidesDeletedFiles() {
        File file = dir().createFile("a");
        screen()
                .assertListViewContains(file, true)
                .assertListViewContains(delete(file), false);
    }

    public void testUpdatesViewOnChildDirectoryModified() throws Exception {
        testUpdatesDateViewOnChildModified(dir().createDir("a"));
    }

    public void testUpdatesViewOnChildFileModified() throws Exception {
        testUpdatesDateViewOnChildModified(dir().createFile("a"));
        testUpdatesSizeViewOnChildModified(dir().createFile("a"));
    }

    private void testUpdatesSizeViewOnChildModified(File f) throws IOException {
        assertTrue(f.setLastModified(0));

        final CharSequence[] size = {null};
        screen().assertFileSizeView(f, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertFalse(isNullOrEmpty(input.toString()));
                size[0] = input;
            }
        });

        modify(f);

        screen().assertFileSizeView(f, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertNotEqual(size[0], input);
            }
        });
    }

    private void testUpdatesDateViewOnChildModified(File f) throws IOException {
        assertTrue(f.setLastModified(0));

        final CharSequence[] date = {null};
        screen().assertFileModifiedDateView(f, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertFalse(isNullOrEmpty(input.toString()));
                date[0] = input;
            }
        });

        modify(f);

        screen().assertFileModifiedDateView(f, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertNotEqual(date[0], input);
            }
        });
    }

    private File delete(File file) {
        assertTrue(file.delete());
        return file;
    }

    private File modify(File file) throws IOException {
        long lastModifiedBefore = file.lastModified();
        if (file.isDirectory()) {
            assertTrue(new File(file, String.valueOf(System.nanoTime())).mkdir());
        } else {
            append("test", file, UTF_8);
        }
        long lastModifiedAfter = file.lastModified();
        assertNotEqual(lastModifiedBefore, lastModifiedAfter);
        return file;
    }
}
