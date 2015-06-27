package l.files.provider.bookmarks;

import android.content.SharedPreferences;

import com.google.common.collect.ImmutableSet;

import l.files.fs.DefaultResourceProvider;
import l.files.fs.Resource;
import l.files.fs.local.ResourceBaseTest;

import static android.content.Context.MODE_PRIVATE;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.provider.bookmarks.BookmarkManager.BookmarkChangedListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class BookmarkManagerTest extends ResourceBaseTest
{
    private BookmarkManagerImpl manager;
    private SharedPreferences pref;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        pref = getContext().getSharedPreferences("bookmark-test", MODE_PRIVATE);
        manager = new BookmarkManagerImpl(DefaultResourceProvider.INSTANCE, pref);
    }

    @Override
    protected void tearDown() throws Exception
    {
        assertTrue(pref.edit().clear().commit());
        super.tearDown();
    }

    public void test_can_add_bookmarks() throws Exception
    {
        final Resource a = dir1().resolve("a").createDirectory();
        final Resource b = dir2().resolve("b").createDirectory();
        manager.addBookmark(a);
        manager.addBookmark(b);
        assertTrue(manager.getBookmarks().containsAll(asList(a, b)));
    }

    public void test_can_remove_bookmarks() throws Exception
    {
        final Resource a = dir1().resolve("a").createDirectory();
        final Resource b = dir1().resolve("b").createDirectory();
        final Resource c = dir1().resolve("c").createDirectory();
        manager.addBookmark(a);
        manager.addBookmark(b);
        manager.addBookmark(c);
        manager.removeBookmark(a);
        manager.removeBookmark(c);
        assertFalse(manager.hasBookmark(a));
        assertTrue(manager.hasBookmark(b));
        assertFalse(manager.hasBookmark(c));
    }

    public void test_notifies_on_bookmark_change() throws Exception
    {
        final BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
        manager.registerBookmarkChangedListener(listener);
        manager.addBookmark(dir1());
        verify(listener).onBookmarkChanged(manager);
    }

    public void test_does_not_notify_removed_listener() throws Exception
    {
        final BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
        manager.registerBookmarkChangedListener(listener);
        manager.unregisterBookmarkChangedListener(listener);
        manager.addBookmark(dir1());
        verify(listener, never()).onBookmarkChanged(manager);
    }

    public void test_removes_non_existing_bookmarks() throws Exception
    {
        final Resource a = dir1().resolve("a").createDirectory();
        final Resource b = dir1().resolve("b").createDirectory();
        manager.addBookmark(a);
        manager.addBookmark(b);
        assertEquals(ImmutableSet.of(a, b), manager.loadBookmarks());

        a.delete();
        assertEquals(singleton(b), manager.loadBookmarks());
    }

}
