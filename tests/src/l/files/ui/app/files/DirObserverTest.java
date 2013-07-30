package l.files.ui.app.files;

import android.os.Handler;
import android.test.AndroidTestCase;
import l.files.test.TempDir;

import java.io.File;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.lang.Thread.sleep;
import static l.files.ui.app.files.DirObserver.BATCH_UPDATE_DELAY;
import static org.mockito.Mockito.*;

public final class DirObserverTest extends AndroidTestCase {

  private Handler handler = new Handler();
  private Runnable listener;
  private TempDir monitored;
  private TempDir unmonitored;

  private DirObserver observer;

  @Override protected void setUp() throws Exception {
    super.setUp();
    monitored = TempDir.create();
    unmonitored = TempDir.create();
    listener = mock(Runnable.class);
    observer = new DirObserver(monitored.get(), handler, listener);
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    observer.stopWatching();
    monitored.delete();
    unmonitored.delete();
  }

  public void testNotifiesOnFileModification() throws Exception {
    File file = monitored.newFile();

    observer.startWatching();
    write("test", file, UTF_8);

    waitForUpdate();
    verify(listener).run();
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

  public void testBatchNotifiesOnFilesModification() throws Exception {
    File file1 = monitored.newFile();
    File file2 = monitored.newFile();

    observer.startWatching();
    write("test1", file1, UTF_8);
    write("test2", file2, UTF_8);

    waitForUpdate();
    verify(listener, times(1)).run();
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
