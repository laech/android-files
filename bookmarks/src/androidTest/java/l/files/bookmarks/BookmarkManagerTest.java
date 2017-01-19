package l.files.bookmarks;

import android.content.SharedPreferences;

import java.util.HashSet;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static android.content.Context.MODE_PRIVATE;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.bookmarks.BookmarkManager.BookmarkChangedListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class BookmarkManagerTest extends PathBaseTest {

    private BookmarkManagerImpl manager;
    private SharedPreferences pref;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pref = getContext().getSharedPreferences("bookmark-test", MODE_PRIVATE);
        manager = new BookmarkManagerImpl(pref);
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(pref.edit().clear().commit());
        super.tearDown();
    }

    public void test_can_add_bookmarks() throws Exception {
        Path a = dir1().concat("a").createDir();
        Path b = dir2().concat("b").createDir();
        manager.addBookmark(a);
        manager.addBookmark(b);
        assertTrue(manager.getBookmarks().containsAll(asList(a, b)));
    }

    public void test_can_remove_bookmarks() throws Exception {
        Path a = dir1().concat("a").createDir();
        Path b = dir1().concat("b").createDir();
        Path c = dir1().concat("c").createDir();
        manager.addBookmark(a);
        manager.addBookmark(b);
        manager.addBookmark(c);
        manager.removeBookmark(a);
        manager.removeBookmark(c);
        assertFalse(manager.hasBookmark(a));
        assertTrue(manager.hasBookmark(b));
        assertFalse(manager.hasBookmark(c));
    }

    public void test_notifies_on_bookmark_change() throws Exception {
        BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
        manager.registerBookmarkChangedListener(listener);
        manager.addBookmark(dir1());
        verify(listener).onBookmarkChanged(manager);
    }

    public void test_does_not_notify_removed_listener() throws Exception {
        BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
        manager.registerBookmarkChangedListener(listener);
        manager.unregisterBookmarkChangedListener(listener);
        manager.addBookmark(dir1());
        verify(listener, never()).onBookmarkChanged(manager);
    }

    public void test_removes_non_existing_bookmarks() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path dir = dir1().concat("dir").createDir();
        Path link = dir1().concat("link").createSymbolicLink(file);
        manager.addBookmark(file);
        manager.addBookmark(dir);
        manager.addBookmark(link);
        assertEquals(new HashSet<>(asList(file, link, dir)), manager.loadBookmarks());

        file.delete();
        assertEquals(singleton(dir), manager.loadBookmarks());
    }

}
