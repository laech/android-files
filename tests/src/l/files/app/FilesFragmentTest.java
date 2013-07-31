package l.files.app;

import android.content.Intent;
import android.os.FileObserver;
import android.test.UiThreadTest;
import android.view.ActionMode;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.event.ViewEvent;
import l.files.test.TempDir;
import l.files.test.TestFilesFragmentActivity;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.event.Sort.DATE_MODIFIED;
import static l.files.event.Sort.NAME;
import static l.files.test.Activities.rotate;
import static l.files.test.TestFilesFragmentActivity.DIRECTORY;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    return getActivity().getFragment();
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
  }

  @UiThreadTest public void testDirObserverIsStoppedOnPause() {
    fragment().observer = mock(FileObserver.class);
    fragment().onResume();
    fragment().onPause();
    verify(fragment().observer).stopWatching();
  }

  public void testViewEventHandlerMethodIsAnnotated() throws Exception {
    Method method = FilesFragment.class.getMethod("handle", ViewEvent.class);
    assertThat(method.getAnnotation(Subscribe.class)).isNotNull();
  }

  public void testHiddenFilesCanBeHidden() throws Throwable {
    dir.newFile(".hidden");
    File expected = dir.newFile("shown");
    post(new ViewEvent(NAME, false));
    assertThat(getFiles()).containsExactly(expected);
  }

  public void testHiddenFilesCanBeShown() throws Throwable {
    File hidden = dir.newFile(".hidden");
    File shown = dir.newFile("shown");
    post(new ViewEvent(NAME, true));
    assertThat(getFiles()).containsExactly(hidden, shown);
  }

  public void testFilesCanBeSorted() throws Throwable {
    final File file1 = dir.newFile(".hidden");
    final File file2 = dir.newFile("shown");
    assertTrue(file1.setLastModified(0));
    assertTrue(file2.setLastModified(10000));
    post(new ViewEvent(DATE_MODIFIED, true));
    assertThat(getFiles()).isEqualTo(asList(file2, file1));
  }

  @UiThreadTest public void testShowsCorrectNumberOfSelectedItemsOnRotation() {
    dir.newFile();

    listView().setItemChecked(0, true);
    rotate(getActivity());

    assertThat(actionMode()).hasTitle(string(R.string.n_selected, 1));
  }

  @UiThreadTest public void testShowsCorrectNumSelectedItemsOnSelection() {
    dir.newFile();
    dir.newFile();

    listView().setItemChecked(0, true);
    listView().setItemChecked(1, true);

    assertThat(actionMode()).hasTitle(string(R.string.n_selected, 2));
  }

  public void testHidesEmptyViewIfDirHasFile() throws Exception {
    dir.newFile();
    assertEmptyViewIsNotVisible();
  }

  public void testShowsEmptyListViewIfDirHasNoFile() {
    assertThat(listView()).hasChildCount(0);
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

  private void assertEmptyViewIsNotVisible() {
    assertThat(emptyView()).hasVisibility(GONE);
  }

  private void assertEmptyViewIsVisible(int msgId) {
    assertThat(emptyView()).hasVisibility(VISIBLE);
    assertThat(emptyView()).hasText(msgId);
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

  private void post(final ViewEvent event) throws Throwable {
    final CountDownLatch latch = new CountDownLatch(1);
    final FilesFragment fragment = fragment();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        fragment.handle(event);
        fragment.getListView().post(new Runnable() {
          @Override public void run() {
            latch.countDown();
          }
        });
      }
    });
    latch.await(5, SECONDS);
  }
}
