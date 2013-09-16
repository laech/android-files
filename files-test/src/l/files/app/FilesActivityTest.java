package l.files.app;

import static l.files.app.FilesActivity.EXTRA_DIR;
import static l.files.app.FilesPagerAdapter.POSITION_FILES;
import static l.files.app.FilesPagerAdapter.POSITION_SIDEBAR;
import static l.files.test.Activities.rotate;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.test.UiThreadTest;
import android.view.ActionMode;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.lang.reflect.Method;
import l.files.common.base.Consumer;
import l.files.test.BaseActivityTest;
import l.files.test.TempDir;

public final class FilesActivityTest
    extends BaseActivityTest<FilesActivity> {

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

  @UiThreadTest public void testFinishesActionModeOnRequest() {
    activity().currentActionMode = mock(ActionMode.class);
    activity().handle(CloseActionModeRequest.INSTANCE);
    verify(activity().currentActionMode).finish();
    verifyNoMoreInteractions(activity().currentActionMode);
  }

  @UiThreadTest public void testFinishesActionModeOnRequestWillSkipIfNoActionMode() {
    activity().currentActionMode = null;
    activity().handle(CloseActionModeRequest.INSTANCE);
    // No error
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

  @SuppressWarnings("unchecked") public void testOpenFileRequestIsHandled() {
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

  @SuppressWarnings("unchecked")//
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
