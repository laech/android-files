package l.files.features;

import l.files.fs.Resource;
import l.files.fs.local.LocalResource;
import l.files.test.BaseFilesActivityTest;

public final class BookmarkSidebarTest extends BaseFilesActivityTest {

    public void testBookmarksSidebarLockedOnBookmarksActionMode() throws Exception {
        Resource a = LocalResource.create(dir().createDir("a"));

        screen()
                .selectItem(a)
                .bookmark()
                .openBookmarksDrawer()
                .checkBookmark(a, true)
                .getActivityObject()
                .assertBookmarksSidebarIsOpenLocked(true);
    }

    public void testDeleteBookmarksFromSidebar() throws Exception {
        Resource a = LocalResource.create(dir().createDir("a"));
        Resource b = LocalResource.create(dir().createDir("b"));
        Resource c = LocalResource.create(dir().createDir("c"));

        screen()

                .selectItem(a).bookmark().pressBack()
                .selectItem(b).bookmark().pressBack()
                .selectItem(c).bookmark().pressBack()

                .openBookmarksDrawer()
                .assertBookmarked(a, true)
                .assertBookmarked(b, true)
                .assertBookmarked(c, true)

                .checkBookmark(a, true)
                .checkBookmark(b, true)
                .deleteCheckedBookmarks()

                .assertBookmarked(a, false)
                .assertBookmarked(b, false)
                .assertBookmarked(c, true);
    }

    public void testBookmarkAppearsInSidebar() throws Exception {
        screen()
                .selectItem(dir().createDir("a"))
                .bookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(true)
                .getActivityObject()
                .unbookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(false);
    }

    public void testBookmarksAreSortedByName() throws Exception {
        Resource b = LocalResource.create(dir().createDir("b"));
        Resource a = LocalResource.create(dir().createDir("a"));
        Resource c = LocalResource.create(dir().createDir("c"));
        screen()
                .selectItem(a).bookmark().pressBack()
                .selectItem(c).bookmark().pressBack()
                .selectItem(b).bookmark()
                .openBookmarksDrawer()
                .assertCurrentDirectoryBookmarked(true)
                .assertContainsBookmarksInOrder(a, b, c);
    }

}
