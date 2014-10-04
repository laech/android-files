package l.files.features;

import java.io.File;

import l.files.test.BaseFilesActivityTest;

public final class BookmarkMenuTest extends BaseFilesActivityTest {

  public void testBookmarkMenuIsUncheckedForNonBookmarkedDirectory() {
    File dir1 = dir().createDir("Not bookmarked 1");
    File dir2 = dir().createDir("Not bookmarked 2");
    screen()
        .selectItem(dir1)
        .assertBookmarkMenuChecked(false)
        .pressBack()
        .selectItem(dir2)
        .assertBookmarkMenuChecked(false);
  }

  public void testBookmarkMenuIsCheckedForBookmarkedDirectory() {
    File dir = dir().createDir("Bookmarked");
    screen()
        .selectItem(dir)
        .bookmark()
        .assertBookmarkMenuChecked(true);
  }

  public void testBookmarkUnbookmarkDirectoryChecksBookmarkMenuCorrectly() {
    File dir = dir().createDir("Bookmarked then unbookmarked");
    screen()
        .selectItem(dir)
        .bookmark()
        .assertBookmarkMenuChecked(true)
        .unbookmark()
        .assertBookmarkMenuChecked(false);
  }

  public void testNavigateThroughBookmarkedUnbookmarkedDirectoriesChecksBookmarkMenuCorrectly() {
    File bookmarked = dir().createDir("Bookmarked");
    File unbookmarked = dir().createDir("Bookmarked/Unbookmarked");
    screen()
        .selectItem(bookmarked)
        .bookmark()
        .assertBookmarkMenuChecked(true)
        .selectItem(unbookmarked)
        .assertBookmarkMenuChecked(false)
        .pressBack()
        .assertBookmarkMenuChecked(true)
        .selectItem(unbookmarked)
        .assertBookmarkMenuChecked(false);
  }

}
