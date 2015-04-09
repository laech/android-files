package l.files.provider.bookmarks;

import android.content.SharedPreferences;

import java.io.File;

import l.files.common.testing.BaseTest;
import l.files.fs.DefaultResourceProvider;
import l.files.fs.Resource;
import l.files.fs.local.LocalResource;

import static android.content.Context.MODE_PRIVATE;
import static java.util.Arrays.asList;
import static l.files.provider.bookmarks.BookmarkManager.BookmarkChangedListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class BookmarkManagerTest extends BaseTest {

    private BookmarkManager manager;
    private SharedPreferences pref;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pref = getContext().getSharedPreferences("bookmark-test", MODE_PRIVATE);
        manager = new BookmarkManagerImpl(DefaultResourceProvider.INSTANCE, pref);
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(pref.edit().clear().commit());
        super.tearDown();
    }

    public void testAddBookmark() throws Exception {
        Resource p1 = LocalResource.create(new File("/a/b"));
        Resource p2 = LocalResource.create(new File("/a/c"));
        manager.addBookmark(p1);
        manager.addBookmark(p2);
        assertTrue(manager.getBookmarks().containsAll(asList(p1, p2)));
    }

    public void testRemoveBookmark() throws Exception {
        Resource p1 = LocalResource.create(new File("/a/b"));
        Resource p2 = LocalResource.create(new File("/1"));
        Resource p3 = LocalResource.create(new File("/x"));
        manager.addBookmark(p1);
        manager.addBookmark(p2);
        manager.addBookmark(p3);
        manager.removeBookmark(p1);
        manager.removeBookmark(p3);
        assertFalse(manager.hasBookmark(p1));
        assertTrue(manager.hasBookmark(p2));
        assertFalse(manager.hasBookmark(p3));
    }

    public void testRemoveBookmarks() throws Exception {
        Resource p1 = LocalResource.create(new File("/a/b"));
        Resource p2 = LocalResource.create(new File("/1"));
        Resource p3 = LocalResource.create(new File("/x"));
        manager.addBookmark(p1);
        manager.addBookmark(p2);
        manager.addBookmark(p3);
        manager.removeBookmarks(asList(p1, p2));
        assertFalse(manager.hasBookmark(p1));
        assertFalse(manager.hasBookmark(p2));
        assertTrue(manager.hasBookmark(p3));
    }

    public void testHasBookmark() throws Exception {
        Resource resource = LocalResource.create(new File("/a/b"));
        assertFalse(manager.hasBookmark(resource));
        manager.addBookmark(resource);
        assertTrue(manager.hasBookmark(resource));
    }

    public void testNotifiesOnBookmarkChanged() throws Exception {
        BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
        manager.registerBookmarkChangedListener(listener);
        manager.addBookmark(LocalResource.create(new File("a")));
        verify(listener).onBookmarkChanged(manager);
    }

    public void testRemoveNotificationOnBookmarkChanged() throws Exception {
        BookmarkChangedListener listener = mock(BookmarkChangedListener.class);
        manager.registerBookmarkChangedListener(listener);
        manager.unregisterBookmarkChangedListener(listener);
        manager.addBookmark(LocalResource.create(new File("a")));
        verify(listener, never()).onBookmarkChanged(manager);
    }

}
