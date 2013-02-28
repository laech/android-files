package com.example.files.ui.activities;

import static com.example.files.test.TempFolder.newTempFolder;
import static com.example.files.ui.activities.FileListActivity.EXTRA_FOLDER;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.nanoTime;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.example.files.test.TempFolder;

public final class FileListActivityTest
    extends ActivityInstrumentationTestCase2<FileListActivity> {

  private static interface FileSetup {
    void setup(File file);
  }

  private List<Activity> activitiesToFinish;
  private TempFolder folder;

  public FileListActivityTest() {
    super(FileListActivity.class);
  }

  public void testClickingOnFolderWillShowItsContent() throws Throwable {
    final File expected = newFileIn(folder.newFolder());
    click(getActivity(), expected.getParentFile());

    final FileListActivity newActivity = waitForActivity(newMonitor(), 2000L);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        assertEquals(expected, firstItem(newActivity));
      }
    });
  }

  public void testClickingOnUnexecutableFolderWillDoNothing() throws Throwable {
    testWillDoNothingOnClick(new FileSetup() {
      @Override public void setup(File file) {
        file.setExecutable(false, false);
      }
    });
  }

  public void testClickingOnUnreadableFolderWillDoNothing() throws Throwable {
    testWillDoNothingOnClick(new FileSetup() {
      @Override public void setup(File file) {
        file.setReadable(false, false);
      }
    });
  }

  public void testSetsFileListFragmentListenerOnResume() {
    getInstrumentation().callActivityOnResume(getActivity());
    assertNotNull(getActivity().getFileListFragment().getListener());
  }

  public void testShowsFolderSpecified() throws Exception {
    File file = folder.newFile();
    setActivityIntent(newIntent(folder.get()));
    assertEquals(file, listView().getItemAtPosition(0));
  }

  public void testUnsetsFileListFragmentListenerOnPause() {
    getInstrumentation().callActivityOnPause(getActivity());
    assertNull(getActivity().getFileListFragment().getListener());
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    folder = newTempFolder();
    activitiesToFinish = newArrayList();
  }

  @Override protected void tearDown() throws Exception {
    try {
      for (Activity activity : activitiesToFinish) {
        activity.finish();
      }
      folder.delete();
    } finally {
      super.tearDown();
    }
  }

  private void click(final FileListActivity activity, final File folder)
      throws Throwable {
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        activity.onFileClick(folder);
      }
    });
  }

  private File firstItem(final FileListActivity activity) {
    return (File)listView(activity).getItemAtPosition(0);
  }

  private ListView listView() {
    return listView(getActivity());
  }

  private ListView listView(FileListActivity activity) {
    return activity.getFileListFragment().getListView();
  }

  private File newFileIn(final File folder) throws IOException {
    final File file = new File(folder, String.valueOf(nanoTime()));
    assertTrue(file.createNewFile());
    return file;
  }

  private Intent newIntent(File folder) {
    return new Intent()
        .putExtra(EXTRA_FOLDER, folder.getAbsolutePath());
  }

  private ActivityMonitor newMonitor() {
    ActivityMonitor monitor = new ActivityMonitor((String)null, null, false);
    getInstrumentation().addMonitor(monitor);
    return monitor;
  }

  private void testWillDoNothingOnClick(FileSetup setup) throws Throwable {
    setActivityIntent(newIntent(folder.get()));
    File subFolder = folder.newFolder();
    setup.setup(subFolder);
    try {
      click(getActivity(), subFolder);
      assertNull(waitForActivity(newMonitor(), 1000L));
    } finally {
      assertTrue(subFolder.delete());
    }
  }

  private FileListActivity waitForActivity(ActivityMonitor mon, long timeout) {
    final Activity activity = mon.waitForActivityWithTimeout(timeout);
    if (activity != null) {
      activitiesToFinish.add(activity);
    }
    return (FileListActivity)activity;
  }
}
