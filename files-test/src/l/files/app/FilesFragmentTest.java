package l.files.app;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.app.FilesFragment.Event.REFRESH_END;
import static l.files.app.FilesFragment.Event.REFRESH_START;
import static l.files.test.TestFilesFragmentActivity.DIRECTORY;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.os.FileObserver;
import android.test.UiThreadTest;
import android.view.ActionMode;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import l.files.R;
import l.files.event.ShowHiddenFilesSetting;
import l.files.event.SortSetting;
import l.files.sort.Sorters;
import l.files.test.TempDir;
import l.files.test.TestFilesFragmentActivity;

public final class FilesFragmentTest
    extends BaseFileListFragmentTest<TestFilesFragmentActivity> {

  private TempDir dir;

  public FilesFragmentTest() {
    super(TestFilesFragmentActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = TempDir.create();
    setTestIntent(dir.get());
  }

  @Override protected void tearDown() throws Exception {
    dir.delete();
    super.tearDown();
  }

  @Override protected FilesFragment fragment() {
    FilesFragment fragment = getActivity().getFragment();
    fragment.executor = sameThreadExecutor();
    return fragment;
  }

  private List<File> getFiles() {
    List<File> files = newArrayList();
    ListView list = listView();
    for (int i = 0; i < list.getCount(); i++) {
      Object item = list.getItemAtPosition(i);
      if (item instanceof File) files.add((File) item);
    }
    return files;
  }

  @UiThreadTest public void testDirObserverIsStartedOnResume() {
    fragment().observer = mock(FileObserver.class);
    fragment().onResume();
    verify(fragment().observer).startWatching();
    verifyNoMoreInteractions(fragment().observer);
  }

  @UiThreadTest public void testDirObserverIsStoppedOnPause() {
    fragment().observer = mock(FileObserver.class);
    fragment().onResume();
    fragment().onPause();
    verify(fragment().observer).startWatching();
    verify(fragment().observer).stopWatching();
    verifyNoMoreInteractions(fragment().observer);
  }

  public void testShowHiddenFilesHandlerMethodIsAnnotated() throws Exception {
    Method method = FilesFragment.class.getMethod("handle", ShowHiddenFilesSetting.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testHiddenFilesCanBeHidden() throws Throwable {
    dir.newFile(".hidden");
    File expected = dir.newFile("shown");
    post(new ShowHiddenFilesSetting(false));
    assertEquals(asList(expected), getFiles());
  }

  public void testHiddenFilesCanBeShown() throws Throwable {
    File hidden = dir.newFile(".hidden");
    File shown = dir.newFile("shown");
    post(new ShowHiddenFilesSetting(true));
    assertEquals(asList(hidden, shown), getFiles());
  }

  public void testSortHandlerMethodIsAnnotated() throws Exception {
    Method method = FilesFragment.class.getMethod("handle", SortSetting.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testFilesCanBeSorted() throws Throwable {
    final File file1 = dir.newFile("a");
    final File file2 = dir.newFile("b");
    assertTrue(file1.setLastModified(0));
    assertTrue(file2.setLastModified(10000));
    post(new SortSetting(Sorters.DATE_MODIFIED));
    assertEquals(asList(file2, file1), getFiles());
  }

  @UiThreadTest public void testShowsCorrectNumSelectedItemsOnSelection() {
    listView().setItemChecked(0, true);
    listView().setItemChecked(1, true);
    assertEquals(string(R.string.n_selected, 2), actionMode().getTitle());
  }

  public void testHidesEmptyViewIfDirHasFile() throws Exception {
    dir.newFile();
    assertEmptyViewIsNotVisible();
  }

  public void testShowsEmptyListViewIfDirHasNoFile() {
    assertEquals(0, listView().getChildCount());
  }

  public void testShowsEmptyMessageIfNoFiles() {
    assertEmptyViewIsVisible(R.string.empty);
  }

  public void testShowsDirNotExistsIfDirectoryDoesNotExist() {
    dir.delete();
    assertEmptyViewIsVisible(R.string.directory_doesnt_exist);
  }

  public void testShowsNotDirMessageIfArgIsNotDir() {
    setTestIntent(dir.newFile());
    assertEmptyViewIsVisible(R.string.not_a_directory);
  }

  public void testShowsEmptyListIfAllFilesAreDeleted() throws Throwable {
    File file = dir.newFile();
    fragment();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        assertEquals(1, listView().getCount());
      }
    });

    assertTrue(file.delete());
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        fragment().refresh(false);
      }
    });
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        assertEquals(0, listView().getCount());
      }
    });
  }

  public void testPostsEventOnRefresh() {
    fragment().setBus(mock(Bus.class));
    fragment().refresh(false);
    verify(fragment().getBus()).post(REFRESH_START);
    verify(fragment().getBus()).post(REFRESH_END);
    verifyNoMoreInteractions(fragment().getBus());
  }

  private void assertEmptyViewIsNotVisible() {
    assertEquals(GONE, emptyView().getVisibility());
  }

  private void assertEmptyViewIsVisible(int msgId) {
    assertEquals(VISIBLE, emptyView().getVisibility());
    assertEquals(string(msgId), emptyView().getText());
  }

  private TextView emptyView() {
    return (TextView) getActivity().findViewById(android.R.id.empty);
  }

  private String string(int resId, Object... args) {
    return getActivity().getString(resId, args);
  }

  private ListView listView() {
    return fragment().getListView();
  }

  private void setTestIntent(File dir) {
    setActivityIntent(new Intent().putExtra(DIRECTORY, dir.getAbsolutePath()));
  }

  private ActionMode actionMode() {
    return getActivity().getActionMode();
  }

  private void post(final Object event) throws Throwable {
    final CountDownLatch latch = new CountDownLatch(1);
    final FilesFragment fragment = fragment();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        if (event instanceof ShowHiddenFilesSetting) {
          fragment.handle((ShowHiddenFilesSetting) event);
        } else {
          fragment.handle((SortSetting) event);
        }
        fragment.getListView().post(new Runnable() {
          @Override public void run() {
            latch.countDown();
          }
        });
      }
    });
    latch.await(5, SECONDS);
  }

  private Executor sameThreadExecutor() {
    return new Executor() {
      @Override public void execute(Runnable command) {
        command.run();
      }
    };
  }
}
