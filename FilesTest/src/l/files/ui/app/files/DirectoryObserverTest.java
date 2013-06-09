package l.files.ui.app.files;

import android.os.Handler;
import android.test.AndroidTestCase;
import l.files.test.TempDirectory;

import java.io.File;

import static java.lang.Thread.sleep;
import static l.files.test.TempDirectory.newTempDirectory;
import static l.files.ui.app.files.DirectoryObserver.BATCH_UPDATE_DELAY;
import static org.mockito.Mockito.*;

public final class DirectoryObserverTest extends AndroidTestCase {

  private Handler handler = new Handler();
  private TempDirectory monitored;
  private TempDirectory unmonitored;
  private Runnable listener;

  private DirectoryObserver observer;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    monitored = newTempDirectory();
    unmonitored = newTempDirectory();
    listener = mock(Runnable.class);
    observer = new DirectoryObserver(monitored.get(), handler, listener);
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    observer.stopWatching();
  }

  public void testNotifiesOnFileAddition() throws Exception {
    observer.startWatching();
    monitored.newFile();
    waitForUpdate();
    verify(listener).run();
  }

  public void testNotifiesOnFileMovedInToMonitoredDir() throws Exception {
    observer.startWatching();
    File from = unmonitored.newFile();
    File to = new File(monitored.get(), from.getName());

    assertTrue(from.renameTo(to));

    waitForUpdate();
    verify(listener).run();
  }

  public void testNotifiesOnFileRemoval() throws Exception {
    File file = monitored.newFile();

    observer.startWatching();
    assertTrue(file.delete());

    waitForUpdate();
    verify(listener).run();
  }

  public void testNotifiesOnFileMovedOutOfMonitoredDir() throws Exception {
    File from = monitored.newFile();
    File to = new File(unmonitored.get(), from.getName());

    observer.startWatching();
    assertTrue(from.renameTo(to));

    waitForUpdate();
    verify(listener).run();
  }

  public void testBatchNotifiesOnFilesAddition() throws Exception {
    observer.startWatching();
    monitored.newFile();
    monitored.newFile();
    waitForUpdate();
    verify(listener, times(1)).run();
  }

  public void testBatchNotifiesOnFilesRemoval() throws Exception {
    File file1 = monitored.newFile();
    File file2 = monitored.newFile();

    observer.startWatching();
    assertTrue(file1.delete());
    assertTrue(file2.delete());

    waitForUpdate();
    verify(listener, times(1)).run();
  }

  private void waitForUpdate() throws InterruptedException {
    sleep(BATCH_UPDATE_DELAY * 2);
  }
}
