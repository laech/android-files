package l.files.ui.bookmarks;

import android.app.LoaderManager;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import l.files.common.testing.BaseActivityTest;
import l.files.fs.DefaultResourceProvider;
import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.fs.local.LocalResource;
import l.files.provider.bookmarks.BookmarkManagerImpl;
import l.files.test.TestActivity;

import static android.app.LoaderManager.LoaderCallbacks;
import static android.content.Context.MODE_PRIVATE;
import static java.util.Collections.singletonList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public final class BookmarksLoaderTest extends BaseActivityTest<TestActivity> {

    private static final Random random = new Random();

    private BookmarkManagerImpl manager;
    private SharedPreferences preferences;

    public BookmarksLoaderTest() {
        super(TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferences = getActivity().getSharedPreferences(getClass().getSimpleName(), MODE_PRIVATE);
        manager = new BookmarkManagerImpl(DefaultResourceProvider.INSTANCE, preferences);
        assertTrue(manager.clearBookmarksSync());
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(preferences.edit().clear().commit());
        super.tearDown();
    }

    public void testSortsBookmarksByName() throws Exception {
        Resource resource = LocalResource.create(new File("/tmp"));
        manager.addBookmark(resource);
        subject().initLoader().awaitOnLoadFinished(singletonList(resource.getPath()));
    }

    public void testNotifiesBookmarkChange() throws Exception {
        Subject subject = subject().initLoader().awaitOnLoadFinished(Collections.<Path>emptyList());

        Resource resource = LocalResource.create(new File("/tmp"));
        manager.addBookmark(resource);
        subject.awaitOnLoadFinished(singletonList(resource.getPath()));

        manager.removeBookmark(resource);
        subject.awaitOnLoadFinished(Collections.<Path>emptyList());
    }

    private Subject subject() {
        int loaderId = random.nextInt();
        LoaderListener listener = mock(LoaderListener.class);
        given(listener.onCreateLoader(eq(loaderId), any(Bundle.class))).will(new Answer<BookmarksLoader>() {
            @Override
            public BookmarksLoader answer(InvocationOnMock invocation) {
                return new BookmarksLoader(getActivity(), manager);
            }
        });
        return new Subject(loaderId, getActivity().getLoaderManager(), listener);
    }

    private static interface LoaderListener extends LoaderCallbacks<List<Path>> {
    }

    private static final class Subject {
        private final int loaderId;
        private final LoaderManager manager;
        private final LoaderListener listener;

        private Subject(int loaderId, LoaderManager manager, LoaderListener listener) {
            this.loaderId = loaderId;
            this.manager = manager;
            this.listener = listener;
        }

        Subject initLoader() {
            manager.initLoader(loaderId, null, listener);
            return this;
        }

        Subject awaitOnLoadFinished(List<Path> expected) {
            verify(listener, timeout(1000)).onLoadFinished(any(BookmarksLoader.class), eq(expected));
            return this;
        }
    }
}
