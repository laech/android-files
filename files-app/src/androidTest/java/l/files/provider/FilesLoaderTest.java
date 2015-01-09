package l.files.provider;

import android.app.LoaderManager;
import android.os.Bundle;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import l.files.common.testing.BaseActivityTest;
import l.files.common.testing.TempDir;
import l.files.fs.FileStatus;
import l.files.fs.FileSystem;
import l.files.fs.FileSystems;
import l.files.fs.Path;
import l.files.fs.WatchService;
import l.files.test.TestActivity;

import static android.app.LoaderManager.LoaderCallbacks;
import static l.files.fs.WatchEvent.Listener;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public final class FilesLoaderTest extends BaseActivityTest<TestActivity> {

  private static final Random random = new Random();

  private TempDir tmp;
  private FileSystem fs;

  public FilesLoaderTest() {
    super(TestActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    tmp = TempDir.create();
    fs = FileSystems.get("file");
  }

  @Override protected void tearDown() throws Exception {
    tmp.delete();
    super.tearDown();
  }

  public void testLoadFiles() throws Exception {
    List<FileStatus> expected = createFiles("a", "b");
    subject().initLoader().awaitOnLoadFinished(expected);
  }

  public void testMonitorsDirectoryChanges() throws Exception {
    List<FileStatus> expected = new ArrayList<>(createFiles("1", "2"));
    Subject subject = subject(fs.getWatchService())
        .initLoader().awaitOnLoadFinished(expected);

    expected.addAll(createFiles("3", "4", "5", "6"));
    subject.awaitOnLoadFinished(expected);
  }

  public void testUnregistersFromWatchServiceOnDestroy() throws Exception {
    WatchService service = mock(WatchService.class);
    ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
    ArgumentCaptor<Listener> listenerCaptor = ArgumentCaptor.forClass(Listener.class);

    Subject subject = subject(service).initLoader();
    verify(service, timeout(2000)).register(pathCaptor.capture(), listenerCaptor.capture());
    assertNotNull(pathCaptor.getValue());
    assertNotNull(listenerCaptor.getValue());

    subject.destroyLoader();
    verify(service, timeout(2000)).unregister(pathCaptor.getValue(), listenerCaptor.getValue());
  }

  private List<FileStatus> createFiles(String... names) {
    List<FileStatus> result = new ArrayList<>();
    for (int i = 0; i < names.length; i++) {
      String name = names[i];
      File file = i % 2 == 0 ? tmp.createFile(name) : tmp.createDir(name);
      result.add(fs.stat(fs.getPath(file.toURI()), false));
    }
    return result;
  }

  Subject subject() {
    return subject(mock(WatchService.class));
  }

  Subject subject(final WatchService service) {
    final int loaderId = random.nextInt();
    final Path root = fs.getPath(tmp.get().toURI());
    final FileSort comparator = FileSort.Name.get();
    final LoaderCallback listener = mock(LoaderCallback.class);
    given(listener.onCreateLoader(eq(loaderId), any(Bundle.class))).will(new Answer<FilesLoader>() {
      @Override public FilesLoader answer(final InvocationOnMock invocation) {
        return new FilesLoader(getActivity(), root, comparator, fs, service);
      }
    });
    return new Subject(loaderId, getActivity().getLoaderManager(), listener);
  }

  private static interface LoaderCallback extends LoaderCallbacks<List<FileStatus>> {}

  private static final class Subject {
    private final int loaderId;
    private final LoaderManager manager;
    private final LoaderCallback listener;

    private Subject(int loaderId, LoaderManager manager, LoaderCallback listener) {
      this.loaderId = loaderId;
      this.manager = manager;
      this.listener = listener;
    }

    Subject awaitOnLoadFinished(List<FileStatus> expected) {
      verify(listener, timeout(2000)).onLoadFinished(any(FilesLoader.class), eq(expected));
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

  }
}
