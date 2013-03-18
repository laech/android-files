package com.example.files.app;

import static android.app.ActionBar.DISPLAY_HOME_AS_UP;
import static android.os.Environment.getExternalStorageDirectory;
import static com.example.files.app.FileListActivity.ARG_DIRECTORY;
import static com.example.files.test.Activities.rotate;
import static com.example.files.test.TempDirectory.newTempDirectory;

import java.io.File;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.example.files.R;
import com.example.files.test.TempDirectory;

public final class FileListActivityTest
    extends ActivityInstrumentationTestCase2<FileListActivity> {

  private TempDirectory directory;

  public FileListActivityTest() {
    super(FileListActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    directory = newTempDirectory();
  }

  @Override protected void tearDown() throws Exception {
    directory.delete();
    super.tearDown();
  }

  public void testShowsCorrectTitleOnRotate() throws Throwable {
    setActivityIntent(newIntent(directory.get()));

    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        rotate(getActivity());
      }
    });

    assertEquals(directory.get().getName(), getActivity().getTitle());
  }

  public void testShowsExternalStorageDirIfNoDirectoryIsSpecified() {
    assertEquals(
        getExternalStorageDirectory(),
        ((File) getListView().getItemAtPosition(0)).getParentFile());

    assertEquals(
        getActivity().getString(R.string.home),
        getActivity().getTitle());
  }

  public void testShowsDirectoryNameAsTitle() {
    setActivityIntent(newIntent(directory.get()));
    assertEquals(directory.get().getName(), getActivity().getTitle());
  }

  public void testShowsDirectorySpecified() {
    File file = directory.newFile();
    setActivityIntent(newIntent(directory.get()));
    assertEquals(file, getListView().getItemAtPosition(0));
  }

  public void testShowsTitleOfDirectorySelectedAndDisplayed() throws Throwable {
    final File dir = show(directory.newDirectory());
    assertEquals(dir.getName(), getActivity().getTitle());
  }

  public void testEnablesHomeButtonOnDisplayOfSubDirectory() throws Throwable {
    show(directory.newDirectory());
    assertTrue(0 < (getActivity().getActionBar().getDisplayOptions() & DISPLAY_HOME_AS_UP));
  }

  private File show(final File dir) throws Throwable {
    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getActivity().show(dir.getAbsolutePath());
      }
    });
    return dir;
  }

  private ListView getListView() {
    return (ListView) getActivity().findViewById(android.R.id.list);
  }

  private Intent newIntent(File directory) {
    return new Intent().putExtra(ARG_DIRECTORY, directory.getAbsolutePath());
  }

}
