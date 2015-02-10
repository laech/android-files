package l.files.features;

import l.files.fs.Path;
import l.files.fs.local.LocalPath;
import l.files.test.BaseFilesActivityTest;

public final class BookmarkSidebarTest extends BaseFilesActivityTest {

  public void testBookmarksSidebarLockedOnBookmarksActionMode() throws Exception {
    Path a = LocalPath.of(dir().createDir("a"));

    screen()
        .selectItem(a)
        .bookmark()
        .openBookmarksDrawer()
        .checkBookmark(a, true)
        .getActivityObject()
        .assertBookmarksSidebarIsOpenLocked(true);
  }

  public void testDeleteBookmarksFromSidebar() throws Exception {
    Path a = LocalPath.of(dir().createDir("a"));
    Path b = LocalPath.of(dir().createDir("b"));
    Path c = LocalPath.of(dir().createDir("c"));

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
    Path b = LocalPath.of(dir().createDir("b"));
    Path a = LocalPath.of(dir().createDir("a"));
    Path c = LocalPath.of(dir().createDir("c"));
    screen()
        .selectItem(a).bookmark().pressBack()
        .selectItem(c).bookmark().pressBack()
        .selectItem(b).bookmark()
        .openBookmarksDrawer()
        .assertCurrentDirectoryBookmarked(true)
        .assertContainsBookmarksInOrder(a, b, c);
  }

}
