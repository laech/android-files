package l.files.app;

import android.content.Intent;
import android.test.UiThreadTest;
import android.view.ActionMode;
import com.squareup.otto.Bus;
import l.files.test.BaseActivityTest;
import l.files.test.TempDir;

import java.io.File;

import static l.files.app.FilesActivity.EXTRA_DIR;
import static org.mockito.Mockito.*;

public final class FilesActivityTest extends BaseActivityTest<FilesActivity> {

    private TempDir mDir;

    public FilesActivityTest() {
        super(FilesActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDir = TempDir.create();
        setActivityIntent(newIntent(mDir.get()));
    }

    @Override
    protected void tearDown() throws Exception {
        mDir.delete();
        super.tearDown();
    }

    @UiThreadTest
    public void testFinishesActionModeOnRequest() {
        activity().mCurrentActionMode = mock(ActionMode.class);
        activity().handle(CloseActionModeRequest.INSTANCE);
        verify(activity().mCurrentActionMode).finish();
        verifyNoMoreInteractions(activity().mCurrentActionMode);
    }

    @UiThreadTest
    public void testFinishesActionModeOnRequestWillSkipIfNoActionMode() {
        activity().mCurrentActionMode = null;
        activity().handle(CloseActionModeRequest.INSTANCE);
        // No error
    }

    @UiThreadTest
    public void testBusIsRegisteredOnResume() throws Throwable {
        FilesActivity activity = getActivity();
        activity.mBus = mock(Bus.class);

        getInstrumentation().callActivityOnResume(activity);

        verify(activity.mBus).register(activity);
    }

    @UiThreadTest
    public void testBusIsUnregisteredOnPause() throws Throwable {
        FilesActivity activity = getActivity();
        activity.mBus = mock(Bus.class);

        getInstrumentation().callActivityOnPause(activity);

        verify(activity.mBus).unregister(activity);
    }

    public void testShowsTitleUsingNameOfDirSpecified() {
        assertEquals(mDir.get().getName(), getTitle());
    }

    private Intent newIntent(File dir) {
        return new Intent().putExtra(EXTRA_DIR, dir.getAbsolutePath());
    }

    @SuppressWarnings("ConstantConditions")
    private CharSequence getTitle() {
        return getActivity().getActionBar().getTitle();
    }
}
