package com.example.files.ui.activities;

import static com.example.files.test.TempFolder.newTempFolder;
import static com.example.files.ui.activities.FileListActivity.EXTRA_FOLDER;

import java.io.File;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.example.files.test.TempFolder;

public final class FileListActivityTest
    extends ActivityInstrumentationTestCase2<FileListActivity> {

  private TempFolder folder;

  public FileListActivityTest() {
    super(FileListActivity.class);
  }

  public void testSetsFileListFragmentListenerOnResume() {
    getInstrumentation().callActivityOnResume(getActivity());
    assertNotNull(getActivity().getFileListFragment().getOnFileClickListener());
  }

  public void testShowsFolderSpecified() throws Exception {
    File file = folder.newFile();
    setActivityIntent(new Intent()
        .putExtra(EXTRA_FOLDER, folder.get().getAbsolutePath()));

    assertEquals(file, listItem(0));
  }

  public void testUnsetsFileListFragmentListenerOnPause() {
    getInstrumentation().callActivityOnPause(getActivity());
    assertNull(getActivity().getFileListFragment().getOnFileClickListener());
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    folder = newTempFolder();
  }

  @Override protected void tearDown() throws Exception {
    try {
      folder.delete();
    } finally {
      super.tearDown();
    }
  }

  private Object listItem(int i) {
    return getActivity().getFileListFragment().getListView()
        .getItemAtPosition(i);
  }
}
