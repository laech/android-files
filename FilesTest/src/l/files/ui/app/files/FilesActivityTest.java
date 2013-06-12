package l.files.ui.app.files;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import com.squareup.otto.Bus;
import l.files.test.TempDirectory;
import l.files.ui.event.FileSelectedEvent;
import l.files.ui.event.MediaDetectedEvent;

import java.io.File;

import static l.files.test.Activities.rotate;
import static l.files.test.TempDirectory.newTempDirectory;
import static l.files.ui.app.files.FilesActivity.EXTRA_DIRECTORY;
import static l.files.ui.app.home.HomePagerAdapter.POSITION_FILES;
import static l.files.ui.app.home.HomePagerAdapter.POSITION_SIDEBAR;
import static org.mockito.Mockito.*;


public class FilesActivityTest<T extends FilesActivity>
    extends ActivityInstrumentationTestCase2<T> {

  private TempDirectory directory;

  @SuppressWarnings("unchecked")
  public FilesActivityTest() {
    super((Class<T>) FilesActivity.class);
  }

  public FilesActivityTest(Class<T> clazz) {
    super(clazz);
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
    getActivity().bus = mock(Bus.class);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnResume(getActivity());
      }
    });
    verify(getActivity().bus).register(getActivity());
  }

  public void testRegistersMediaDetectedEventHandlerOnResume() throws Throwable {
    getActivity().bus = mock(Bus.class);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnResume(getActivity());
      }
    });
    verify(getActivity().bus).register(getActivity());
  }

  public void testUnregistersFileSelectedEventHandlerOnPause() throws Throwable {
    getActivity().bus = mock(Bus.class);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnPause(getActivity());
      }
    });
    verify(getActivity().bus).unregister(getActivity());
  }

  public void testUnregistersMediaDetectedHandlerOnPause() throws Throwable {
    getActivity().bus = mock(Bus.class);
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnPause(getActivity());
      }
    });
    verify(getActivity().bus).unregister(getActivity());
  }

  public void testCallsHelperToHandleFileSelectedEvent() {
    setActivityIntent(newIntent(directory.newDirectory()));
    getActivity().helper = mock(FilesActivityHelper.class);
    FileSelectedEvent event = new FileSelectedEvent(directory.get());
    getActivity().handle(event);
    verify(getActivity().helper).handle(event, getActivity());
  }

  public void testCallsHelperToHandleMediaDetectedEvent() {
    getActivity().helper = mock(FilesActivityHelper.class);
    MediaDetectedEvent event = new MediaDetectedEvent(directory.get(), "a");
    getActivity().handle(event);
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
    directory.newFile();
    setActivityIntent(newIntent(directory.get()));
    assertEquals(directory.get(), anyFile().getParentFile());
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
        activity.handle(new FileSelectedEvent(dir));
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

  private File anyFile() {
    ListView list = getListView();
    for (int i = 0; i < list.getCount(); i++) {
      Object item = list.getItemAtPosition(i);
      if (item instanceof File) return (File) item;
    }
    throw new AssertionError("No file found");
  }
}
