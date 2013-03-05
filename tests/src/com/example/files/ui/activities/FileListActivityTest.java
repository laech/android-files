package com.example.files.ui.activities;

import static com.example.files.test.TempFolder.newTempFolder;
import static com.example.files.ui.activities.FileListActivity.ARG_FOLDER;

import java.io.File;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.example.files.test.TempFolder;

public final class FileListActivityTest
    extends ActivityInstrumentationTestCase2<FileListActivity> {

  private TempFolder folder;

  public FileListActivityTest() {
    super(FileListActivity.class);
  }

  public void testShowsFolderSpecified() {
    File file = folder.newFile();
    setActivityIntent(newIntent(folder.get()));
    assertEquals(file, listView().getItemAtPosition(0));
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    folder = newTempFolder();
  }

  @Override protected void tearDown() throws Exception {
    folder.delete();
    super.tearDown();
  }

  private ListView listView() {
    return getActivity().getFileListFragment().getListView();
  }

  private Intent newIntent(File folder) {
    return new Intent().putExtra(ARG_FOLDER, folder.getAbsolutePath());
  }

}
