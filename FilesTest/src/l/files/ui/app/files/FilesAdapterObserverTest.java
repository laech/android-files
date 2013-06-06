package l.files.ui.app.files;

import android.os.Handler;
import android.test.AndroidTestCase;
import l.files.test.TempDirectory;
import l.files.ui.widget.UpdatableAdapter;

import java.io.File;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static l.files.test.TempDirectory.newTempDirectory;
import static l.files.ui.app.files.FilesAdapterObserver.BATCH_UPDATE_DELAY;
import static l.files.util.FileSort.BY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class FilesAdapterObserverTest extends AndroidTestCase {

  private Handler handler = new Handler();
  private TempDirectory monitored;
  private TempDirectory unmonitored;
  private UpdatableAdapter<File> adapter;

  private FilesAdapterObserver observer;

  @SuppressWarnings("unchecked")
  @Override protected void setUp() throws Exception {
    super.setUp();
    monitored = newTempDirectory();
    unmonitored = newTempDirectory();
    adapter = mock(UpdatableAdapter.class);
    observer = new FilesAdapterObserver(monitored.get(), adapter, handler);
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    observer.stopWatching();
  }

  public void testNotifiesOnFileAddition() throws Exception {
    observer.startWatching();
    File file = monitored.newFile();
    waitForUpdate();
    verify(adapter).addAll(asList(file), BY_NAME);
  }

  public void testNotifiesOnFileMovedInToMonitoredDir() throws Exception {
    observer.startWatching();
    File from = unmonitored.newFile();
    File to = new File(monitored.get(), from.getName());

    assertTrue(from.renameTo(to));

    waitForUpdate();
    verify(adapter).addAll(asList(to), BY_NAME);
  }

  public void testNotifiesOnFileRemoval() throws Exception {
    File file = monitored.newFile();

    observer.startWatching();
    assertTrue(file.delete());

    waitForUpdate();
    verify(adapter).removeAll(asList(file));
  }

  public void testNotifiesOnFileMovedOutOfMonitoredDir() throws Exception {
    File from = monitored.newFile();
    File to = new File(unmonitored.get(), from.getName());

    observer.startWatching();
    assertTrue(from.renameTo(to));

    waitForUpdate();
    verify(adapter).removeAll(asList(from));
  }

  public void testBatchNotifiesOnFilesAddition() throws Exception {
    observer.startWatching();
    File file1 = monitored.newFile();
    File file2 = monitored.newFile();
    waitForUpdate();
    verify(adapter).addAll(asList(file1, file2), BY_NAME);
  }

  public void testBatchNotifiesOnFilesRemoval() throws Exception {
    File file1 = monitored.newFile();
    File file2 = monitored.newFile();

    observer.startWatching();
    assertTrue(file1.delete());
    assertTrue(file2.delete());

    waitForUpdate();
    verify(adapter).removeAll(asList(file1, file2));
  }

  private void waitForUpdate() throws InterruptedException {
    sleep(BATCH_UPDATE_DELAY * 2);
  }
}
