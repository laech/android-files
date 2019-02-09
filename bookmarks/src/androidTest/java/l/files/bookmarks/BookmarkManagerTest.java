package l.files.bookmarks;

import android.content.SharedPreferences;

import org.junit.Test;

import java.util.HashSet;

import l.files.fs.Path;
import l.files.testing.fs.PathBaseTest;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.InstrumentationRegistry.getContext;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.bookmarks.BookmarkManager.BookmarkChangedListener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class BookmarkManagerTest extends PathBaseTest {

    private BookmarkManagerImpl manager;
    private SharedPreferences pref;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        pref = getContext().getSharedPreferences("bookmark-test", MODE_PRIVATE);
        manager = new BookmarkManagerImpl(pref);
    }

    @Override
    public void tearDown() throws Exception {
        assertTrue(pref.edit().clear().commit());
        super.tearDown();
    }

    @Test
    public void can_add_bookmarks() throws Exception {
        Path a = dir1().concat("a").createDirectory();
        Path b = dir2().concat("b").createDirectory();
        manager.addBookmark(a);
        manager.addBookmark(b);
        assertTrue(manager.getBookmarks().containsAll(asList(a, b)));
    }

    @Test
    public void can_remove_bookmarks() throws Exception {
        Path a = dir1().concat("a").createDirectory();
        Path b = dir1().concat("b").createDirectory();
        Path c = dir1().concat("c").createDirectory();
        manager.addBookmark(a);
        manager.addBookmark(b);
        manager.addBookmark(c);
        manager.removeBookmark(a);
        manager.removeBookmark(c);
        assertFalse(manager.hasBookmark(a));
        assertTrue(manager.hasBookmark(b));
        assertFalse(manager.hasBookmark(c));
    }

    @Test
    public void notifies_on_bookmark_change() throws Exception {
        BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
        manager.registerBookmarkChangedListener(listener);
        manager.addBookmark(dir1());
        verify(listener).onBookmarkChanged(manager);
    }

    @Test
    public void does_not_notify_removed_listener() throws Exception {
        BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
        manager.registerBookmarkChangedListener(listener);
        manager.unregisterBookmarkChangedListener(listener);
        manager.addBookmark(dir1());
        verify(listener, never()).onBookmarkChanged(manager);
    }

    @Test
    public void removes_non_existing_bookmarks() throws Exception {
        Path file = dir1().concat("file").createFile();
        Path dir = dir1().concat("dir").createDirectory();
        Path link = dir1().concat("link").createSymbolicLink(file);
        manager.addBookmark(file);
        manager.addBookmark(dir);
        manager.addBookmark(link);
        assertEquals(new HashSet<>(asList(file, link, dir)), manager.loadBookmarks());

        file.delete();
        assertEquals(singleton(dir), manager.loadBookmarks());
    }

}
