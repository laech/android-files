package l.files.provider;

import android.os.Bundle;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public final class FilesLoaderTest extends BaseActivityTest<TestActivity> {

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

  private LoaderCallback mockLoaderCallbacks(FilesLoader loader) {
    LoaderCallback listener = mock(LoaderCallback.class);
    given(listener.onCreateLoader(anyInt(), any(Bundle.class))).willReturn(loader);
    return listener;
  }

  Subject subject() {
    return subject(mock(WatchService.class));
  }

  Subject subject(WatchService service) {
    Path root = fs.getPath(tmp.get().toURI());
    FileSort comparator = FileSort.Name.get();
    FilesLoader loader = new FilesLoader(getActivity(), root, comparator, service);
    LoaderCallback callbacks = mockLoaderCallbacks(loader);
    return new Subject(loader, callbacks);
  }

  private static interface LoaderCallback extends LoaderCallbacks<List<FileStatus>> {}

  private final class Subject {
    private final FilesLoader loader;
    private final LoaderCallback listener;

    private Subject(FilesLoader loader, LoaderCallback listener) {
      this.loader = loader;
      this.listener = listener;
    }

    Subject awaitOnLoadFinished(List<FileStatus> expected) {
      Mockito.verify(listener, timeout(2000)).onLoadFinished(loader, expected);
      return this;
    }

    Subject initLoader() {
      getActivity().getLoaderManager().initLoader(0, null, listener);
      return this;
    }

    Subject destroyLoader() {
      getActivity().getLoaderManager().destroyLoader(0);
      return this;
    }

  }
}
