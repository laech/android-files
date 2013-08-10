package l.files.app;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.app.menu.SortDialog;
import l.files.common.base.Consumer;
import l.files.test.TempDir;

import java.io.File;
import java.lang.reflect.Method;

import static l.files.app.FilesActivity.EXTRA_DIR;
import static l.files.app.FilesPagerAdapter.POSITION_FILES;
import static l.files.app.FilesPagerAdapter.POSITION_SIDEBAR;
import static l.files.test.Activities.rotate;
import static org.mockito.Mockito.*;

public final class FilesActivityTest
    extends ActivityInstrumentationTestCase2<FilesActivity> {

  private TempDir dir;

  public FilesActivityTest() {
    super(FilesActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = TempDir.create();
    setActivityIntent(newIntent(dir.get()));
  }

  @Override protected void tearDown() throws Exception {
    dir.delete();
    super.tearDown();
  }

  public void testSortDialogIsShownOnSortMenuClick() throws Throwable {
    getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        getInstrumentation().callActivityOnResume(getActivity());
      }
    });

    getInstrumentation().invokeMenuActionSync(getActivity(), R.id.sort_by, 0);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        DialogFragment fragment = (DialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(SortDialog.FRAGMENT_TAG);
        Dialog dialog = fragment.getDialog();
        assertTrue(dialog.isShowing());
      }
    });
  }

  @UiThreadTest public void testBusIsRegisteredOnResume() throws Throwable {
    FilesActivity activity = getActivity();
    activity.bus = mock(Bus.class);

    getInstrumentation().callActivityOnResume(activity);

    verify(activity.bus).register(activity);
  }

  @UiThreadTest public void testBusIsUnregisteredOnPause() throws Throwable {
    FilesActivity activity = getActivity();
    activity.bus = mock(Bus.class);

    getInstrumentation().callActivityOnPause(activity);

    verify(activity.bus).unregister(activity);
  }

  @SuppressWarnings("unchecked")
  public void testOpenFileRequestIsHandled() {
    FilesActivity activity = getActivity();
    activity.helper = mock(Consumer.class);
    OpenFileRequest event = new OpenFileRequest(dir.newDir());

    activity.handle(event);

    verify(activity.helper).take(event);
  }

  public void testOpenFileRequestHandlerMethodIsAnnotated() throws Exception {
    Method method = FilesActivity.class.getMethod("handle", OpenFileRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  // TODO verify this actually works
  public void testShowsTitleCorrectlyOnScreenRotate() throws Throwable {
    final FilesActivity activity = getActivity();
    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        rotate(activity);
      }
    });

    assertEquals(dir.get().getName(), title());
  }

  public void testShowsTitleUsingNameOfDirSpecified() {
    assertEquals(dir.get().getName(), title());
  }

  @SuppressWarnings("unchecked")
  public void testScrollsToFilesViewIfDirSpecifiedIsAlreadyDisplayed() throws Throwable {
    dir.newFile();
    final FilesActivity activity = getActivity();
    activity.helper = mock(Consumer.class);

    runTestOnUiThread(new Runnable() {
      @Override public void run() {
        activity.pager.setCurrentItem(POSITION_SIDEBAR);
        activity.handle(new OpenFileRequest(dir.get()));
      }
    });

    assertEquals(POSITION_FILES, getActivity().pager.getCurrentItem());
    verifyZeroInteractions(activity.helper);
  }

  private Intent newIntent(File dir) {
    return new Intent().putExtra(EXTRA_DIR, dir.getAbsolutePath());
  }

  private CharSequence title() {
    return getActivity().getActionBar().getTitle();
  }
}
