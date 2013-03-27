package com.example.files.app;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.event.FileSelectedEvent;
import com.example.files.test.TempDirectory;
import com.example.files.test.TestFileListFragmentActivity;
import com.squareup.otto.Bus;

import java.io.File;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.files.test.Activities.rotate;
import static com.example.files.test.TempDirectory.newTempDirectory;
import static com.example.files.test.TestFileListFragmentActivity.DIRECTORY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class FilesFragmentTest
    extends ActivityInstrumentationTestCase2<TestFileListFragmentActivity> {

  private TempDirectory directory;

  public FilesFragmentTest() {
    super(TestFileListFragmentActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    directory = newTempDirectory();
    setTestIntent(directory.get());
  }

  @Override protected void tearDown() throws Exception {
    directory.delete();
    super.tearDown();
  }

  public void testSortsFilesByName() {
    File z = directory.newFile("z");
    File a = directory.newFile("a");
    File c = directory.newDirectory("C");

    ListView list = getListView();
    assertEquals(a, list.getItemAtPosition(0));
    assertEquals(c, list.getItemAtPosition(1));
    assertEquals(z, list.getItemAtPosition(2));
  }

  public void testShowsCorrectNumSelectedItemsOnRotation() throws Throwable {
    directory.newFile();

    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getListView().setItemChecked(0, true);
        rotate(getActivity());
      }
    });

    assertEquals(
        getString(R.string.n_selected, 1),
        getActivity().getActionMode().getTitle());
  }

  public void testShowsCorrectNumSelectedItemsOnSelection() throws Throwable {
    directory.newFile();
    directory.newFile();

    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getListView().setItemChecked(0, true);
        getListView().setItemChecked(1, true);
      }
    });

    assertEquals(
        getString(R.string.n_selected, 2),
        getActivity().getActionMode().getTitle());
  }

  public void testHidesEmptyViewIfDirectoryHasFile() throws Exception {
    directory.newFile();
    assertEmptyViewIsNotVisible();
  }

  public void testPostsEventOnItemClick() throws Throwable {
    final File file = directory.newFile();
    Bus bus = getActivity().getFragment().bus = mock(Bus.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        clickFirstListItem();
      }
    });

    verify(bus).post(new FileSelectedEvent(file));
  }

  public void testShowsEmptyListViewIfDirectoryHasNoFile() {
    assertEquals(0, getListView().getChildCount());
  }

  public void testShowsEmptyMessageIfNoFiles() {
    assertEmptyViewIsVisible(R.string.empty);
  }

  public void testShowsDirectoryNotExistsIfDirectoryDoesNotExist() {
    directory.delete();
    assertEmptyViewIsVisible(R.string.directory_doesnt_exist);
  }

  public void testShowsNotDirectoryMessageIfArgIsNotDirectory() {
    setTestIntent(directory.newFile());
    assertEmptyViewIsVisible(R.string.not_a_directory);
  }

  private void assertEmptyViewIsNotVisible() {
    assertEquals(GONE, getEmptyView().getVisibility());
  }

  private void assertEmptyViewIsVisible(int msgId) {
    assertEquals(VISIBLE, getEmptyView().getVisibility());
    assertEquals(getString(msgId), getEmptyView().getText().toString());
  }

  private void clickFirstListItem() {
    assertTrue(
        getListView().performItemClick(getListView().getChildAt(0), 0, 0));
  }

  private TextView getEmptyView() {
    return (TextView) getActivity().findViewById(android.R.id.empty);
  }

  private FilesFragment getFragment() {
    return getActivity().getFragment();
  }

  private String getString(int resId) {
    return getActivity().getString(resId);
  }

  private String getString(int resId, Object... args) {
    return getActivity().getString(resId, args);
  }

  private ListView getListView() {
    return getFragment().getListView();
  }

  private void setTestIntent(File directory) {
    setActivityIntent(new Intent()
        .putExtra(DIRECTORY, directory.getAbsolutePath()));
  }
}
