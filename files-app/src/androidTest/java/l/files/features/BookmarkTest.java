package l.files.features;

import java.io.File;

import l.files.test.BaseFilesActivityTest;

public final class BookmarkTest extends BaseFilesActivityTest {

  public void testBookmarkMenuIsUncheckedForNonBookmarkedDirectory() {
    File dir1 = dir().createDir("Not bookmarked 1");
    File dir2 = dir().createDir("Not bookmarked 2");
    screen()
        .selectItem(dir1)
        .assertBookmarked(false)
        .pressBack()
        .selectItem(dir2)
        .assertBookmarked(false);
  }

  public void testBookmarkMenuIsCheckedForBookmarkedDirectory() {
    File dir = dir().createDir("Bookmarked");
    screen()
        .selectItem(dir)
        .bookmark()
        .assertBookmarked(true);
  }

  public void testBookmarkUnbookmarkDirectoryChecksBookmarkMenuCorrectly() {
    File dir = dir().createDir("Bookmarked then unbookmarked");
    screen()
        .selectItem(dir)
        .bookmark()
        .assertBookmarked(true)
        .unbookmark()
        .assertBookmarked(false);
  }

  public void testNavigateThroughBookmarkedUnbookmarkedDirectoriesChecksBookmarkMenuCorrectly() {
    File bookmarked = dir().createDir("Bookmarked");
    File unbookmarked = dir().createDir("Bookmarked/Unbookmarked");
    screen()
        .selectItem(bookmarked)
        .bookmark()
        .assertBookmarked(true)
        .selectItem(unbookmarked)
        .assertBookmarked(false)
        .pressBack()
        .assertBookmarked(true)
        .selectItem(unbookmarked)
        .assertBookmarked(false);
  }

}
