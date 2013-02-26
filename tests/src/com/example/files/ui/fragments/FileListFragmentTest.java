package com.example.files.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.files.test.TempFolder.newTempFolder;

import java.io.File;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.test.TestFileListFragmentActivity;
import com.example.files.test.TempFolder;
import com.example.files.ui.fragments.FileListFragment.OnFileClickListener;

public final class FileListFragmentTest
    extends ActivityInstrumentationTestCase2<TestFileListFragmentActivity> {

  private TempFolder folder;

  public FileListFragmentTest() {
    super(TestFileListFragmentActivity.class);
  }

  public void testHidesEmptyViewIfFolderHasFile() throws Exception {
    folder.newFile();
    assertEmptyViewIsNotVisible();
  }

  public void testPostsEventOnItemClick() throws Throwable {
    final File expected = folder.newFile();
    final int[] clicked = {0};
    final OnFileClickListener tester = new OnFileClickListener() {
      @Override public void onFileClick(File actual) {
        clicked[0]++;
        assertEquals(expected, actual);
      }
    };

    final TestFileListFragmentActivity activity = getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        activity.getFragment().setOnFileClickListener(tester);
        assertTrue(listView().performItemClick(listView().getChildAt(0), 0, 0));
      }
    });

    assertEquals(1, clicked[0]);
  }

  public void testShowsEmptyListViewIfFolderHasNoFile() {
    assertEquals(0, listView().getChildCount());
  }

  public void testShowsEmptyMessageIfNoFiles() {
    assertEmptyViewIsVisible(R.string.empty);
  }

  public void testShowsFolderNotExistsIfFolderDoesntExist() throws Exception {
    folder.delete();
    assertEmptyViewIsVisible(R.string.folder_doesnt_exist);
  }

  public void testShowsNotFolderMessageIfArgIsNotFolder() throws Exception {
    setTestIntent(folder.newFile());
    assertEmptyViewIsVisible(R.string.not_a_folder);
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
    assertEquals(GONE, emptyView().getVisibility());
  }

  private void assertEmptyViewIsVisible(int msgId) {
    assertEquals(VISIBLE, emptyView().getVisibility());
    assertEquals(getString(msgId), emptyView().getText().toString());
  }

  private TextView emptyView() {
    return (TextView)getActivity().findViewById(android.R.id.empty);
  }

  private String getString(int resId) {
    return getActivity().getString(resId);
  }

  private ListView listView() {
    return (ListView)getActivity().findViewById(android.R.id.list);
  }

  private void setTestIntent(File folder) {
    setActivityIntent(new Intent()
        .putExtra(TestFileListFragmentActivity.FOLDER, folder.getAbsolutePath()));
  }
}
