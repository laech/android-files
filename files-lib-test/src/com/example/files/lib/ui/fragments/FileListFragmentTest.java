package com.example.files.lib.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.files.lib.test.TempFolder.newTempFolder;
import static com.example.files.lib.ui.fragments.FileListFragment.fileListFragment;
import static com.example.files.lib.ui.fragments.FileListFragmentTest.TestActivity.EXTRA_FOLDER;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;

import com.example.files.lib.test.R;
import com.example.files.lib.test.TempFolder;
import com.example.files.lib.ui.fragments.FileListFragmentTest.TestActivity;

public final class FileListFragmentTest
    extends ActivityInstrumentationTestCase2<TestActivity> {

  public static class TestActivity extends Activity {
    static final String EXTRA_FOLDER = "folder";

    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.content);

      String folder = getIntent().getStringExtra(EXTRA_FOLDER);
      getFragmentManager()
          .beginTransaction()
          .replace(android.R.id.content, fileListFragment(folder))
          .commit();
    }
  }

  private TempFolder folder;

  public FileListFragmentTest() {
    super(TestActivity.class);
  }

  public void testHidesEmptyViewIfFolderHasFile() throws Exception {
    folder.newFile();
    assertEmptyViewIsNotVisible();
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
        .putExtra(EXTRA_FOLDER, folder.getAbsolutePath()));
  }
}
