package l.files.features;

import java.io.IOException;
import java.io.Writer;

import l.files.common.base.Consumer;
import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.test.BaseFilesActivityTest;

import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class NavigationTest extends BaseFilesActivityTest {

    public void testDirectoryViewIsDisabledIfNoReadPermission() throws Exception {
        Resource dir = directory().resolve("dir").createDirectory();
        dir.removePermissions(Permission.allRead());
        screen().assertDisabled(dir);
    }

    public void testSymbolicLinkIconDisplayed() throws Exception {
        Resource dir = directory().resolve("dir").createDirectory();
        Resource link = directory().resolve("link").createLink(dir);

        screen()
                .assertSymbolicLinkIconDisplayed(dir, false)
                .assertSymbolicLinkIconDisplayed(link, true);
    }

    public void testCanNavigateIntoSymlinkDirectory() throws Exception {
        Resource dir = directory().resolve("dir").createDirectory();
        dir.resolve("a").createDirectory();

        Resource link = directory().resolve("link").createLink(dir);
        Resource linkChild = link.resolve("a");
        screen()
                .selectItem(link)
                .selectItem(linkChild)
                .assertCurrentDirectory(linkChild);
    }

    public void testCanSeeChangesInSymlinkDirectory() throws Exception {
        Resource dir = directory().resolve("dir").createDirectory();
        Resource link = directory().resolve("link").createLink(dir);
        screen().selectItem(link)
                .assertCurrentDirectory(link);

        Resource child = link.resolve("child").createDirectory();
        screen().selectItem(child)
                .assertCurrentDirectory(child);

    }

    public void testPressActionBarUpIndicatorWillGoBack() throws Exception {
        Resource dir = directory().resolve("dir").createDirectory();
        screen()
                .selectItem(dir)
                .assertCurrentDirectory(dir)
                .pressActionBarUpIndicator()
                .assertCurrentDirectory(dir.parent());
    }

    public void testActionBarTitleShowsNameOfDirectory() throws Exception {
        screen()
                .selectItem(directory().resolve("a").createDirectory())
                .assertActionBarTitle("a");
    }

    public void testActionBarHidesUpIndicatorWhenThereIsNoBackStackInitially() {
        screen().assertActionBarUpIndicatorIsVisible(false);
    }

    public void testActionBarShowsUpIndicatorWhenThereIsBackStack() throws Exception {
        screen()
                .selectItem(directory().resolve("dir").createDirectory())
                .assertActionBarUpIndicatorIsVisible(true);
    }

    public void testActionBarHidesUpIndicatorWhenThereIsNoBackStackToGoBackTo() throws Exception {
        screen()
                .selectItem(directory().resolve("dir").createDirectory())
                .pressBack()
                .assertActionBarUpIndicatorIsVisible(false);
    }

    public void testLongPressBackWillClearBackStack() throws Exception {
        screen()
                .selectItem(directory().resolve("a").createDirectory())
                .selectItem(directory().resolve("a/b").createDirectory())
                .selectItem(directory().resolve("a/b/c").createDirectory())
                .longPressBack()
                .assertCurrentDirectory(directory());
    }

    public void testOpenNewDirectoryWillCloseOpenedDrawer() throws Exception {
        Resource dir = directory().resolve("a").createDirectory();
        screen()
                .openBookmarksDrawer()
                .getActivityObject()
                .selectItem(dir)
                .assertDrawerIsOpened(false);
    }

    public void testObservesOnCurrentDirectoryAndShowsNewlyAddedFiles() throws Exception {
        Resource dir = directory().resolve("a").createDirectory();
        screen().assertListViewContains(dir, true);
    }

    public void testObservesOnCurrentDirectoryAndHidesDeletedFiles() throws Exception {
        Resource file = directory().resolve("a").createFile();
        screen().assertListViewContains(file, true);
        file.delete();
        screen().assertListViewContains(file, false);
    }

    public void testUpdatesViewOnChildDirectoryModified() throws Exception {
        Resource dir = directory().resolve("a").createDirectory();
        testUpdatesDateViewOnChildModified(dir);
    }

    public void testUpdatesViewOnChildFileModified() throws Exception {
        Resource file = directory().resolve("a").createFile();
        testUpdatesDateViewOnChildModified(file);
        testUpdatesSizeViewOnChildModified(file);
    }

    private void testUpdatesSizeViewOnChildModified(Resource resource) throws IOException {
        resource.setModificationTime(NOFOLLOW, EPOCH);

        final CharSequence[] size = {null};
        screen().assertFileSizeView(resource, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertFalse(isNullOrEmpty(input.toString()));
                size[0] = input;
            }
        });

        modify(resource);

        screen().assertFileSizeView(resource, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertNotEqual(size[0], input);
            }
        });
    }

    private void testUpdatesDateViewOnChildModified(Resource resource) throws IOException {
        resource.setModificationTime(NOFOLLOW, EPOCH);

        final CharSequence[] date = {null};
        screen().assertFileModifiedDateView(resource, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertFalse(isNullOrEmpty(input.toString()));
                date[0] = input;
            }
        });

        modify(resource);

        screen().assertFileModifiedDateView(resource, new Consumer<CharSequence>() {
            @Override
            public void apply(CharSequence input) {
                assertNotEqual(date[0], input);
            }
        });
    }

    private Resource modify(Resource resource) throws IOException {
        Stat stat = resource.stat(NOFOLLOW);
        Instant lastModifiedBefore = stat.modificationTime();
        if (stat.isDirectory()) {
            resource.resolve(String.valueOf(System.nanoTime())).createDirectory();
        } else {
            try (Writer writer = resource.writer(NOFOLLOW, UTF_8, true)) {
                writer.write("test");
            }
        }
        Instant lastModifiedAfter = resource.stat(NOFOLLOW).modificationTime();
        assertNotEqual(lastModifiedBefore, lastModifiedAfter);
        return resource;
    }

}
