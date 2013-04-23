package l.files.util;

import android.os.Handler;
import junit.framework.TestCase;
import l.files.event.EventBus;
import l.files.test.TempDirectory;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.io.Files.createTempDir;
import static java.io.File.createTempFile;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.test.TempDirectory.newTempDirectory;
import static org.mockito.Mockito.mock;

public final class DirectoryObserverTest extends TestCase {

  private TempDirectory monitored;
  private TempDirectory unmonitored;
  private CountDownLatch latch;
  private DirectoryObserverTester observer;

  @Override protected void setUp() throws Exception {
    super.setUp();
    monitored = newTempDirectory();
    unmonitored = newTempDirectory();
    latch = new CountDownLatch(1);
    observer = new DirectoryObserverTester(monitored.get(), mock(EventBus.class), new Handler()) {
      @Override public void onEvent(int eventType, String path) {
        super.onEvent(eventType, path);
        latch.countDown();
      }
    };
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    observer.stopWatching();
    monitored.delete();
    unmonitored.delete();
  }

  public void testCreateFileIsNotified() throws Exception {
    testCreateIsNotified(true, new Runnable() {
      @Override public void run() {
        monitored.newFile();
      }
    });
  }

  public void testCreateFileInSubDirIsNotNotified() throws Exception {
    final TempDirectory subDir = monitored.newSubTempDirectory();
    testCreateIsNotified(false, new Runnable() {
      @Override public void run() {
        subDir.newFile();
      }
    });
  }

  public void testCreateDirIsNotified() throws Exception {
    testCreateIsNotified(true, new Runnable() {
      @Override public void run() {
        monitored.newDirectory();
      }
    });
  }

  public void testCreateDirInSubDirIsNotNotified() throws Exception {
    final TempDirectory subDir = monitored.newSubTempDirectory();
    testCreateIsNotified(false, new Runnable() {
      @Override public void run() {
        subDir.newDirectory();
      }
    });
  }

  private void testCreateIsNotified(boolean notified, Runnable createFile) throws Exception {
    assertEquals(0, observer.count.get());
    observer.startWatching();
    createFile.run();
    awaitEvent();
    assertEquals(notified ? 1 : 0, observer.count.get());
  }

  public void testDeleteFileIsNotified() {
    testDeleteIsNotified(true, monitored.newFile());
  }

  public void testDeleteFileInSubDirIsNotNotified() throws Exception {
    File file = monitored.newSubTempDirectory().newFile();
    testDeleteIsNotified(false, file);
  }

  public void testDeleteDirIsNotified() {
    testDeleteIsNotified(true, monitored.newDirectory());
  }

  public void testDeleteDirInSubDirIsNotNotified() throws Exception {
    File dir = monitored.newSubTempDirectory().newDirectory();
    testDeleteIsNotified(false, dir);
  }

  private void testDeleteIsNotified(boolean notified, File file) {
    assertTrue(file.exists());
    observer.startWatching();
    assertTrue(file.delete());
    awaitEvent();
    assertEquals(notified ? 1 : 0, observer.count.get());
  }

  public void testMoveFileToMonitoredDirIsNotified() throws Exception {
    testMoveIsNotified(true, createTempFile("abc", "123"), new File(monitored.get(), "1"));
  }

  public void testMoveDirToMonitoredDirIsNotified() throws Exception {
    testMoveIsNotified(true, createTempDir(), new File(monitored.get(), "1"));
  }

  public void testMoveFileFromMonitoredDirIsNotified() throws Exception {
    testMoveIsNotified(true, monitored.newFile(), new File(unmonitored.get(), "1"));
  }

  public void testMoveDirFromMonitoredDirIsNotified() throws Exception {
    testMoveIsNotified(true, monitored.newDirectory(), new File(unmonitored.get(), "1"));
  }

  public void testMoveFileFromSubDirIsNotNotified() throws Exception {
    File to = new File(unmonitored.get(), "1");
    File from = new File(monitored.newDirectory(), "1");
    assertTrue(from.createNewFile());
    testMoveIsNotified(false, from, to);
  }

  public void testMoveFileToSubDirIsNotNotified() throws Exception {
    testMoveIsNotified(false, unmonitored.newFile(), new File(monitored.newDirectory(), "1"));
  }

  public void testMoveDirFromSubDirIsNotNotified() throws Exception {
    File to = new File(unmonitored.get(), "1");
    File from = new File(monitored.newDirectory(), "1");
    assertTrue(from.mkdir());
    testMoveIsNotified(false, from, to);
  }

  public void testMoveDirToSubDirIsNotNotified() throws Exception {
    testMoveIsNotified(false, unmonitored.newDirectory(), new File(monitored.newDirectory(), "1"));
  }

  private void testMoveIsNotified(boolean notified, File from, File to) throws Exception {
    assertTrue(from.exists());
    assertFalse(to.exists());
    observer.startWatching();
    assertTrue(from.renameTo(to));
    awaitEvent();
    assertEquals(notified ? 1 : 0, observer.count.get());
  }

  private void awaitEvent() {
    try {
      latch.await(5, MILLISECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static class DirectoryObserverTester extends DirectoryObserver {

    final AtomicInteger count = new AtomicInteger(0);

    public DirectoryObserverTester(File directory, EventBus bus, Handler handler) {
      super(directory, bus, handler);
    }

    @Override public void onEvent(int eventType, String path) {
      super.onEvent(eventType, path);
      count.incrementAndGet();
    }
  }

}
