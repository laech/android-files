package l.files.app.os;

import android.os.AsyncTask;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import l.files.common.os.AsyncTaskExecutor;
import l.files.event.ShowHiddenFilesSetting;
import l.files.event.SortSetting;
import l.files.sort.Sorters;
import l.files.test.BaseTest;
import l.files.test.TempDir;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;
import static l.files.app.os.Monitor.Callback;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DefaultMonitorTest extends BaseTest {

    private TempDir mDir;
    private Bus mBus;
    private DefaultMonitor mMonitor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDir = TempDir.create();
        mDir.newFile();
        mDir.newDir();
        mBus = mock(Bus.class);
        mMonitor = DefaultMonitor.create(mBus, getContext().getResources(), new RefreshTaskExecutor());
        mMonitor.handle(new ShowHiddenFilesSetting(true));
        mMonitor.handle(new SortSetting(Sorters.NAME));
    }

    @Override
    protected void tearDown() throws Exception {
        mDir.delete();
        super.tearDown();
    }

    public void testRegistersToBus() {
        verify(mBus).register(mMonitor);
    }

    public void testRegister() {
        Callback callback1 = mock(Callback.class);
        Callback callback2 = mock(Callback.class);
        File dir = mDir.get();

        mMonitor.register(callback1, dir);
        mMonitor.register(callback2, dir);

        assertTrue(mMonitor.getCallbacks(dir).contains(callback1));
        assertTrue(mMonitor.getCallbacks(dir).contains(callback2));
        assertNotNull(mMonitor.getObserver(dir));

        final Optional<List<Object>> contents = mMonitor.getContents(dir);
        verify(callback1).onRefreshed(contents);
        verify(callback2).onRefreshed(contents);
        assertTrue(contents.isPresent());
        assertTrue(contents.get().containsAll(asList(dir.listFiles())));
    }

    public void testUnregister() {
        Callback callback = mock(Callback.class);
        File dir = new File("/");
        mMonitor.register(callback, dir);
        mMonitor.unregister(callback, dir);

        assertTrue(mMonitor.getCallbacks(dir).isEmpty());
        assertNull(mMonitor.getObserver(dir));
        assertNull(mMonitor.getContents(dir));
    }

    static class RefreshTaskExecutor implements AsyncTaskExecutor {
        @Override
        public <Params> void execute(AsyncTask<Params, ?, ?> t, Params... params) {
            DefaultMonitor.RefreshTask task = (DefaultMonitor.RefreshTask) t;
            task.onPostExecute(task.doInBackground());
        }
    }
}

