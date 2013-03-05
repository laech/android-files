package com.example.files.ui.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.files.test.TempFolder.newTempFolder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;

import org.mockito.ArgumentCaptor;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.test.TempFolder;
import com.example.files.test.TestFileListFragmentActivity;
import com.example.files.ui.events.FileClickEvent;
import com.squareup.otto.Bus;

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

  public void testPostsNoEventOnItemClickIfItemIsDisabled() throws Throwable {
    folder.newFile();
    Bus bus = getActivity().getFragment().bus = mock(Bus.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        listView().getChildAt(0).setEnabled(false);
      }
    });

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        clickFirstListItem();
      }
    });

    verifyZeroInteractions(bus);
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

  private void clickFirstListItem() {
    assertTrue(listView().performItemClick(listView().getChildAt(0), 0, 0));
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

  private ArgumentCaptor<FileClickEvent> newArgumentCaptor() {
    return ArgumentCaptor.forClass(FileClickEvent.class);
  }

  private void setTestIntent(File folder) {
    setActivityIntent(new Intent().putExtra(
        TestFileListFragmentActivity.FOLDER, folder.getAbsolutePath()));
  }
}
