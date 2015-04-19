package l.files.ui.browser;

import android.app.LoaderManager;
import android.os.Bundle;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import l.files.common.testing.BaseActivityTest;
import l.files.common.testing.TempDir;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.fs.local.LocalResource;
import l.files.test.TestActivity;

import static android.app.LoaderManager.LoaderCallbacks;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class FilesLoaderTest extends BaseActivityTest<TestActivity> {

    private static final Random random = new Random();

    private TempDir tmp;
    private Resource resource;

    public FilesLoaderTest() {
        super(TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tmp = TempDir.create();
        resource = LocalResource.create(tmp.get());
    }

    @Override
    protected void tearDown() throws Exception {
        tmp.delete();
        super.tearDown();
    }

    public void testLoadFiles() throws Exception {
        List<FileListItem.File> expected = createFiles("a", "b");
        try (Subject subject = subject()) {
            subject.initLoader().awaitOnLoadFinished(expected);
        }
    }

    public void testMonitorsDirectoryChanges() throws Exception {
        List<FileListItem.File> expected = new ArrayList<>(createFiles("1", "2"));
        try (Subject subject = subject()) {
            subject.initLoader().awaitOnLoadFinished(expected);
            expected.addAll(createFiles("3", "4", "5", "6"));
            subject.awaitOnLoadFinished(expected);
        }
    }

    // TODO
//    public void testUnregistersFromWatchServiceOnDestroy() throws Exception {
//        try (Subject subject = subject()) {
//            subject.initLoader();
//
//            timeout(2, SECONDS, new Runnable() {
//                @Override
//                public void run() {
//                    assertTrue(resource.getResource().getWatcher().isRegistered(resource));
//                }
//            });
//
//            subject.destroyLoader();
//            timeout(2, SECONDS, new Runnable() {
//                @Override
//                public void run() {
//                    assertFalse(resource.getResource().getWatcher().isRegistered(resource));
//                }
//            });
//        }
//    }

    private List<FileListItem.File> createFiles(String... names) throws IOException {
        List<FileListItem.File> result = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (i % 2 == 0) {
                tmp.createFile(name);
            } else {
                tmp.createDir(name);
            }
            Resource child = resource.resolve(name);
            ResourceStatus stat = child.readStatus(false);
            result.add(FileListItem.File.create(child, stat, stat));
        }
        return result;
    }

    Subject subject() {
        final int loaderId = random.nextInt();
        final LoaderCallback listener = mock(LoaderCallback.class);
        given(listener.onCreateLoader(eq(loaderId), any(Bundle.class))).will(new Answer<FilesLoader>() {
            @Override
            public FilesLoader answer(final InvocationOnMock invocation) {
                return new FilesLoader(getActivity(), resource, FileSort.NAME, true);
            }
        });
        return new Subject(loaderId, getActivity().getLoaderManager(), listener);
    }

    private interface LoaderCallback extends LoaderCallbacks<List<FileListItem>> {
    }

    private static final class Subject implements AutoCloseable {
        private final int loaderId;
        private final LoaderManager manager;
        private final LoaderCallback listener;

        private Subject(int loaderId, LoaderManager manager, LoaderCallback listener) {
            this.loaderId = loaderId;
            this.manager = manager;
            this.listener = listener;
        }

        @SuppressWarnings("unchecked")
        Subject awaitOnLoadFinished(List<?> expected) {
            verify(listener, Mockito.timeout(2000))
                    .onLoadFinished(any(FilesLoader.class), (List<FileListItem>) eq(expected));
            return this;
        }

        Subject initLoader() {
            manager.initLoader(loaderId, null, listener);
            return this;
        }

        Subject destroyLoader() {
            manager.destroyLoader(loaderId);
            return this;
        }

        @Override
        public void close() {
            destroyLoader();
        }
    }

}
