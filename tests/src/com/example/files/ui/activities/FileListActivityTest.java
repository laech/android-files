package com.example.files.ui.activities;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import com.example.files.R;
import com.example.files.test.TempFolder;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static com.example.files.test.Activities.rotate;
import static com.example.files.test.TempFolder.newTempFolder;
import static com.example.files.ui.activities.FileListActivity.ARG_FOLDER;

public final class FileListActivityTest
    extends ActivityInstrumentationTestCase2<FileListActivity> {

  private TempFolder folder;

  public FileListActivityTest() {
    super(FileListActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    folder = newTempFolder();
  }

  @Override protected void tearDown() throws Exception {
    folder.delete();
    super.tearDown();
  }

  public void testShowsCorrectTitleOnRotate() throws Throwable {
    setActivityIntent(newIntent(folder.get()));

    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        rotate(getActivity());
      }
    });

    assertEquals(folder.get().getName(), getActivity().getTitle());
  }

  public void testShowsExternalStorageDirIfNoFolderIsSpecified() {
    assertEquals(
        getExternalStorageDirectory().getAbsolutePath(),
        getActivity().getIntent().getStringExtra(ARG_FOLDER));

    assertEquals(
        getActivity().getString(R.string.home),
        getActivity().getTitle());
  }

  public void testShowsFolderNameAsTitle() {
    setActivityIntent(newIntent(folder.get()));
    assertEquals(folder.get().getName(), getActivity().getTitle());
  }

  public void testShowsFolderSpecified() {
    File file = folder.newFile();
    setActivityIntent(newIntent(folder.get()));
    assertEquals(file, getListView().getItemAtPosition(0));
  }

  private ListView getListView() {
    return (ListView) getActivity().findViewById(android.R.id.list);
  }

  private Intent newIntent(File folder) {
    return new Intent().putExtra(ARG_FOLDER, folder.getAbsolutePath());
  }

}
