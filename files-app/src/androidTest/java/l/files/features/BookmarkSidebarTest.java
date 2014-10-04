package l.files.features;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import l.files.test.BaseFilesActivityTest;

import static java.util.Collections.sort;

public final class BookmarkSidebarTest extends BaseFilesActivityTest {

  public void testBookmarkAppearsInSidebar() throws Exception {
    screen()
        .selectItem(dir().createDir("a"))
        .bookmark()
        .assertBookmarkSidebarHasCurrentDirectory(true)
        .unbookmark()
        .assertBookmarkSidebarHasCurrentDirectory(false);
  }

  public void testBookmarksAreSortedByName() throws Exception {
    File a = dir().createDir("a");
    File b = dir().createDir("b");
    File c = dir().createDir("c");
    List<String> bookmarks = screen()
        .selectItem(a).bookmark().pressBack()
        .selectItem(c).bookmark().pressBack()
        .selectItem(b).bookmark()
        .assertBookmarkSidebarHasCurrentDirectory(true)
        .getSidebarBookmarkNames();

    List<String> copy = new ArrayList<>(bookmarks);
    sort(copy, Collator.getInstance());
    assertEquals(copy, bookmarks);
    assertTrue(bookmarks.contains(a.getName()));
    assertTrue(bookmarks.contains(b.getName()));
    assertTrue(bookmarks.contains(c.getName()));
  }
}
