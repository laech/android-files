package l.files.app.os;

import android.os.Handler;
import android.test.AndroidTestCase;
import l.files.test.TempDir;

import java.io.File;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.lang.Thread.sleep;
import static l.files.app.os.DirObserver.BATCH_UPDATE_DELAY;
import static org.mockito.Mockito.*;

public final class DirObserverTest extends AndroidTestCase {

    private Handler mHandler = new Handler();
    private Runnable mListener;
    private TempDir mMonitoredDir;
    private TempDir mUnmonitoredDir;

    private DirObserver mObserver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMonitoredDir = TempDir.create();
        mUnmonitoredDir = TempDir.create();
        mListener = mock(Runnable.class);
        mObserver = new DirObserver(mMonitoredDir.get(), mHandler, mListener);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mObserver.stopWatching();
        mMonitoredDir.delete();
        mUnmonitoredDir.delete();
    }

    public void testNotifiesOnFileModification() throws Exception {
        File file = mMonitoredDir.newFile();

        mObserver.startWatching();
        write("test", file, UTF_8);

        waitForUpdate();
        verify(mListener).run();
    }

    public void testNotifiesOnFileAddition() throws Exception {
        mObserver.startWatching();
        mMonitoredDir.newFile();
        waitForUpdate();
        verify(mListener).run();
    }

    public void testNotifiesOnFileMovedInToMonitoredDir() throws Exception {
        mObserver.startWatching();
        File from = mUnmonitoredDir.newFile();
        File to = new File(mMonitoredDir.get(), from.getName());

        assertTrue(from.renameTo(to));

        waitForUpdate();
        verify(mListener).run();
    }

    public void testNotifiesOnFileRemoval() throws Exception {
        File file = mMonitoredDir.newFile();

        mObserver.startWatching();
        assertTrue(file.delete());

        waitForUpdate();
        verify(mListener).run();
    }

    public void testNotifiesOnFileMovedOutOfMonitoredDir() throws Exception {
        File from = mMonitoredDir.newFile();
        File to = new File(mUnmonitoredDir.get(), from.getName());

        mObserver.startWatching();
        assertTrue(from.renameTo(to));

        waitForUpdate();
        verify(mListener).run();
    }

    public void testBatchNotifiesOnFilesModification() throws Exception {
        File file1 = mMonitoredDir.newFile();
        File file2 = mMonitoredDir.newFile();

        mObserver.startWatching();
        write("test1", file1, UTF_8);
        write("test2", file2, UTF_8);

        waitForUpdate();
        verify(mListener, times(1)).run();
    }

    public void testBatchNotifiesOnFilesAddition() throws Exception {
        mObserver.startWatching();
        mMonitoredDir.newFile();
        mMonitoredDir.newFile();
        waitForUpdate();
        verify(mListener, times(1)).run();
    }

    public void testBatchNotifiesOnFilesRemoval() throws Exception {
        File file1 = mMonitoredDir.newFile();
        File file2 = mMonitoredDir.newFile();

        mObserver.startWatching();
        assertTrue(file1.delete());
        assertTrue(file2.delete());

        waitForUpdate();
        verify(mListener, times(1)).run();
    }

    private void waitForUpdate() throws InterruptedException {
        sleep(BATCH_UPDATE_DELAY * 2);
    }
}
