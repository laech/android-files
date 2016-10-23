package l.files.bookmarks;

import android.content.SharedPreferences;

import java.util.HashSet;

import l.files.fs.Files;
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
        Path a = Files.createDir(dir1().concat("a"));
        Path b = Files.createDir(dir2().concat("b"));
        manager.addBookmark(a);
        manager.addBookmark(b);
        assertTrue(manager.getBookmarks().containsAll(asList(a, b)));
    }

    public void test_can_remove_bookmarks() throws Exception {
        Path a = Files.createDir(dir1().concat("a"));
        Path b = Files.createDir(dir1().concat("b"));
        Path c = Files.createDir(dir1().concat("c"));
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
        Path file = Files.createFile(dir1().concat("file"));
        Path dir = Files.createDir(dir1().concat("dir"));
        Path link = Files.createSymbolicLink(dir1().concat("link"), file);
        manager.addBookmark(file);
        manager.addBookmark(dir);
        manager.addBookmark(link);
        assertEquals(new HashSet<>(asList(file, link, dir)), manager.loadBookmarks());

        Files.delete(file);
        assertEquals(singleton(dir), manager.loadBookmarks());
    }

}
