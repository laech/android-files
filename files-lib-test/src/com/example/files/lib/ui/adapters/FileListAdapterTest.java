package com.example.files.lib.ui.adapters;

import static com.example.files.lib.test.TempFolder.newTempFolder;
import static com.example.files.lib.ui.adapters.FileListAdapterTest.TestActivity.EXTRA_FOLDER;

import java.io.File;
import java.io.IOException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.files.lib.test.TempFolder;
import com.example.files.lib.ui.adapters.FileListAdapterTest.TestActivity;

public final class FileListAdapterTest
    extends ActivityInstrumentationTestCase2<TestActivity> {

  public static class TestActivity extends ListActivity {
    static final String EXTRA_FOLDER = "folder";

    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      String folder = getIntent().getStringExtra(EXTRA_FOLDER);
      setListAdapter(new FileListAdapter(this, new File(folder).listFiles()));
    }
  }

  private TempFolder folder;

  public FileListAdapterTest() {
    super(TestActivity.class);
  }

  public void testViewIsDisabledForUnreadableFile() throws Exception {
    createReadableFile(false);
    assertFalse(firstListViewItem().isEnabled());
  }

  public void testViewIsEnabledForReadableFile() throws Exception {
    createReadableFile(true);
    assertTrue(firstListViewItem().isEnabled());
  }

  public void testViewShowsFileName() throws Exception {
    File file = folder.newFile();
    assertEquals(file.getName(), firstListViewItemText());
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

  private File createReadableFile(boolean readable) throws IOException {
    File file = folder.newFile();
    file.setReadable(readable, false);
    return file;
  }

  private View firstListViewItem() {
    return listView().getChildAt(0);
  }

  private String firstListViewItemText() {
    return ((TextView)firstListViewItem().findViewById(android.R.id.text1))
        .getText().toString();
  }

  private ListView listView() {
    return (ListView)getActivity().findViewById(android.R.id.list);
  }

  private void setTestIntent(File folder) {
    setActivityIntent(new Intent()
        .putExtra(EXTRA_FOLDER, folder.getAbsolutePath()));
  }
}
