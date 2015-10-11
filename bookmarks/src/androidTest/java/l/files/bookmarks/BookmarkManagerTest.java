package l.files.bookmarks;

import android.content.SharedPreferences;

import java.util.HashSet;

import l.files.fs.File;
import l.files.testing.fs.FileBaseTest;

import static android.content.Context.MODE_PRIVATE;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.bookmarks.BookmarkManager.BookmarkChangedListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class BookmarkManagerTest extends FileBaseTest {

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
        File a = dir1().resolve("a").createDir();
        File b = dir2().resolve("b").createDir();
        manager.addBookmark(a);
        manager.addBookmark(b);
        assertTrue(manager.getBookmarks().containsAll(asList(a, b)));
    }

    public void test_can_remove_bookmarks() throws Exception {
        File a = dir1().resolve("a").createDir();
        File b = dir1().resolve("b").createDir();
        File c = dir1().resolve("c").createDir();
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
        File a = dir1().resolve("a").createDir();
        File b = dir1().resolve("b").createDir();
        manager.addBookmark(a);
        manager.addBookmark(b);
        assertEquals(new HashSet<>(asList(a, b)), manager.loadBookmarks());

        a.delete();
        assertEquals(singleton(b), manager.loadBookmarks());
    }

}
