package l.files.provider.bookmarks;

import android.content.SharedPreferences;

import l.files.common.testing.BaseTest;
import l.files.fs.DefaultPathProvider;
import l.files.fs.Path;
import l.files.fs.local.LocalPath;

import static android.content.Context.MODE_PRIVATE;
import static java.util.Arrays.asList;
import static l.files.provider.bookmarks.BookmarkManager.BookmarkChangedListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class BookmarkManagerTest extends BaseTest {

  private BookmarkManager manager;
  private SharedPreferences pref;

  @Override protected void setUp() throws Exception {
    super.setUp();
    pref = getContext().getSharedPreferences("bookmark-test", MODE_PRIVATE);
    manager = new BookmarkManagerImpl(DefaultPathProvider.INSTANCE, pref);
  }

  @Override protected void tearDown() throws Exception {
    assertTrue(pref.edit().clear().commit());
    super.tearDown();
  }

  public void testAddBookmark() throws Exception {
    Path p1 = LocalPath.of("/a/b");
    Path p2 = LocalPath.of("/a/c");
    manager.addBookmark(p1);
    manager.addBookmark(p2);
    assertTrue(manager.getBookmarks().containsAll(asList(p1, p2)));
  }

  public void testRemoveBookmark() throws Exception {
    Path p1 = LocalPath.of("/a/b");
    Path p2 = LocalPath.of("/1");
    Path p3 = LocalPath.of("/x");
    manager.addBookmark(p1);
    manager.addBookmark(p2);
    manager.addBookmark(p3);
    manager.removeBookmark(p1);
    manager.removeBookmark(p3);
    assertFalse(manager.hasBookmark(p1));
    assertTrue(manager.hasBookmark(p2));
    assertFalse(manager.hasBookmark(p3));
  }

  public void testHasBookmark() throws Exception {
    Path path = LocalPath.of("/a/b");
    assertFalse(manager.hasBookmark(path));
    manager.addBookmark(path);
    assertTrue(manager.hasBookmark(path));
  }

  public void testNotifiesOnBookmarkChanged() throws Exception {
    BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
    manager.registerBookmarkChangedListener(listener);
    manager.addBookmark(LocalPath.of("a"));
    verify(listener).onBookmarkChanged(manager);
  }

  public void testRemoveNotificationOnBookmarkChanged() throws Exception {
    BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
    manager.registerBookmarkChangedListener(listener);
    manager.unregisterBookmarkChangedListener(listener);
    manager.addBookmark(LocalPath.of("a"));
    verify(listener, never()).onBookmarkChanged(manager);
  }
}
