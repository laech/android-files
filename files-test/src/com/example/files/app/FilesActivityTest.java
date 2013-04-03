package com.example.files.app;

import static com.example.files.app.FilesActivity.EXTRA_DIRECTORY;
import static com.example.files.app.FilesPagerAdapter.POSITION_FILES;
import static com.example.files.app.FilesPagerAdapter.POSITION_SIDEBAR;
import static com.example.files.test.Activities.rotate;
import static com.example.files.test.TempDirectory.newTempDirectory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.example.files.event.EventBus;
import com.example.files.event.FileSelectedEvent;
import com.example.files.event.MediaDetectedEvent;
import com.example.files.test.TempDirectory;

public final class FilesActivityTest
    extends ActivityInstrumentationTestCase2<FilesActivity> {

  private TempDirectory directory;

  public FilesActivityTest() {
    super(FilesActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    directory = newTempDirectory();
    setActivityIntent(newIntent(directory.get()));
  }

  @Override protected void tearDown() throws Exception {
    directory.delete();
    super.tearDown();
  }

  public void testRegistersFileSelectedEventHandlerOnResume() throws Throwable {
    getActivity().bus = mock(EventBus.class);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnResume(getActivity());
      }
    });
    verify(getActivity().bus).register(
        FileSelectedEvent.class,
        getActivity().fileSelectedEventHandler);
  }

  public void testRegistersMediaDetectedEventHandlerOnResume() throws Throwable {
    getActivity().bus = mock(EventBus.class);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnResume(getActivity());
      }
    });
    verify(getActivity().bus).register(
        MediaDetectedEvent.class,
        getActivity().mediaDetectedEventHandler);
  }

  public void testUnregistersFileSelectedEventHandlerOnPause() throws Throwable {
    getActivity().bus = mock(EventBus.class);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnPause(getActivity());
      }
    });
    verify(getActivity().bus).unregister(getActivity().fileSelectedEventHandler);
  }

  public void testUnregistersMediaDetectedHandlerOnPause() throws Throwable {
    getActivity().bus = mock(EventBus.class);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnPause(getActivity());
      }
    });
    verify(getActivity().bus).unregister(getActivity().mediaDetectedEventHandler);
  }

  public void testCallsHelperToHandleFileSelectedEvent() {
    setActivityIntent(newIntent(directory.newDirectory()));
    getActivity().helper = mock(FilesActivityHelper.class);
    FileSelectedEvent event = new FileSelectedEvent(directory.get());
    getActivity().fileSelectedEventHandler.handle(event);
    verify(getActivity().helper).handle(event, getActivity());
  }

  public void testCallsHelperToHandleMediaDetectedEvent() {
    getActivity().helper = mock(FilesActivityHelper.class);
    MediaDetectedEvent event = new MediaDetectedEvent(directory.get(), "a");
    getActivity().mediaDetectedEventHandler.handle(event);
    verify(getActivity().helper).handle(event, getActivity());
  }

  public void testShowsTitleCorrectlyOnScreenRotate() throws Throwable {
    setActivityIntent(newIntent(directory.get()));

    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        rotate(getActivity());
      }
    });

    assertEquals(directory.get().getName(), getTitle());
  }

  public void testShowsTitleUsingNameOfDirectorySpecified() {
    setActivityIntent(newIntent(directory.get()));
    assertEquals(directory.get().getName(), getTitle());
  }

  public void testShowsDirectorySpecified() {
    File file = directory.newFile();
    setActivityIntent(newIntent(directory.get()));
    assertEquals(file, getListView().getItemAtPosition(0));
  }

  public void testScrollsToFilesViewIfDirectorySpecifiedIsAlreadyDisplayed()
      throws Throwable {
    final File dir = directory.newDirectory();
    final FilesActivity activity = getActivity();
    activity.helper = mock(FilesActivityHelper.class);
    activity.directoryInDisplay = dir;

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        activity.pager.setCurrentItem(POSITION_SIDEBAR);
        activity.fileSelectedEventHandler.handle(new FileSelectedEvent(dir));
      }
    });

    assertEquals(POSITION_FILES, activity.pager.getCurrentItem());
    verifyZeroInteractions(activity.helper);
  }

  private ListView getListView() {
    return (ListView) getActivity().findViewById(android.R.id.list);
  }

  private Intent newIntent(File directory) {
    return new Intent().putExtra(EXTRA_DIRECTORY, directory.getAbsolutePath());
  }

  private CharSequence getTitle() {
    return getActivity().getActionBar().getTitle();
  }
}
