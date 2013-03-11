package com.example.files.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.test.TempFolder;
import com.example.files.test.TestFileListFragmentActivity;
import com.example.files.ui.events.FileClickEvent;
import com.squareup.otto.Bus;
import org.mockito.ArgumentCaptor;

import java.io.File;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.files.test.TempFolder.newTempFolder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class FileListFragmentTest
    extends ActivityInstrumentationTestCase2<TestFileListFragmentActivity> {

  private TempFolder folder;

  public FileListFragmentTest() {
    super(TestFileListFragmentActivity.class);
  }

  public void testShowsCorrectNumSelectedItemsOnRotation() throws Throwable {
    folder.newFile();

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
    folder.newFile();
    folder.newFile();

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

  public void testHidesEmptyViewIfFolderHasFile() throws Exception {
    folder.newFile();
    assertEmptyViewIsNotVisible();
  }

  public void testPostsEventOnItemClick() throws Throwable {
    final File expected = folder.newFile();
    getActivity().getFragment().bus = mock(Bus.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        clickFirstListItem();
      }
    });

    ArgumentCaptor<FileClickEvent> arg = newArgumentCaptor();
    verify(getActivity().getFragment().bus).post(arg.capture());
    assertEquals(expected, arg.getValue().getFile());
    assertEquals(1, arg.getAllValues().size());
  }

  public void testShowsEmptyListViewIfFolderHasNoFile() {
    assertEquals(0, getListView().getChildCount());
  }

  public void testShowsEmptyMessageIfNoFiles() {
    assertEmptyViewIsVisible(R.string.empty);
  }

  public void testShowsFolderNotExistsIfFolderDoesNotExist() throws Exception {
    folder.delete();
    assertEmptyViewIsVisible(R.string.folder_doesnt_exist);
  }

  public void testShowsNotFolderMessageIfArgIsNotFolder() throws Exception {
    setTestIntent(folder.newFile());
    assertEmptyViewIsVisible(R.string.not_a_folder);
  }

  private void rotate(Activity activity) {
    int orientation = activity.getRequestedOrientation();
    activity.setRequestedOrientation(
        orientation == SCREEN_ORIENTATION_LANDSCAPE
            ? SCREEN_ORIENTATION_PORTRAIT
            : SCREEN_ORIENTATION_LANDSCAPE);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    folder = newTempFolder();
    setTestIntent(folder.get());
  }

  @Override protected void tearDown() throws Exception {
    try {
      folder.delete();
    } finally {
      super.tearDown();
    }
  }

  private void assertEmptyViewIsNotVisible() {
    assertEquals(GONE, getEmptyView().getVisibility());
  }

  private void assertEmptyViewIsVisible(int msgId) {
    assertEquals(VISIBLE, getEmptyView().getVisibility());
    assertEquals(getString(msgId), getEmptyView().getText().toString());
  }

  private void clickFirstListItem() {
    assertTrue(getListView().performItemClick(getListView().getChildAt(0), 0, 0));
  }

  private TextView getEmptyView() {
    return (TextView) getActivity().findViewById(android.R.id.empty);
  }

  private FileListFragment getFragment() {
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

  private ArgumentCaptor<FileClickEvent> newArgumentCaptor() {
    return ArgumentCaptor.forClass(FileClickEvent.class);
  }

  private void setTestIntent(File folder) {
    setActivityIntent(new Intent().putExtra(
        TestFileListFragmentActivity.FOLDER, folder.getAbsolutePath()));
  }
}
