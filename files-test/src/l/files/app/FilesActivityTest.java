package l.files.app;

import static l.files.app.FilesActivity.EXTRA_DIR;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.test.UiThreadTest;
import android.view.ActionMode;
import android.view.MenuItem;
import com.squareup.otto.Bus;
import java.io.File;
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

  @UiThreadTest public void testPostsEventOnHomePressed() {
    MenuItem item = mock(MenuItem.class);
    given(item.getItemId()).willReturn(android.R.id.home);
    activity().bus = mock(Bus.class);
    activity().onOptionsItemSelected(item);
    verify(activity().bus).post(OnHomePressedEvent.INSTANCE);
  }

  public void testShowsTitleUsingNameOfDirSpecified() {
    assertEquals(dir.get().getName(), title());
  }

  private Intent newIntent(File dir) {
    return new Intent().putExtra(EXTRA_DIR, dir.getAbsolutePath());
  }

  private CharSequence title() {
    return getActivity().getActionBar().getTitle();
  }
}
