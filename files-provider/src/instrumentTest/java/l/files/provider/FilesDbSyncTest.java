package l.files.provider;

import android.content.Context;

import java.util.concurrent.CountDownLatch;

import l.files.common.testing.BaseTest;
import l.files.common.testing.TempDir;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.provider.FilesDbSync.TestListener;

public final class FilesDbSyncTest extends BaseTest {

  private Context context;
  private TempDir tmp;
  private TempDir helper;
  private String name;
  private FilesDbSync manager;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tmp = TempDir.create();
    helper = TempDir.create();
    name = "test-" + nanoTime();
    context = getContext();
    manager = new FilesDbSync(context, new FilesDb(context, name));
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    manager.shutdown();
    context.deleteDatabase(name);
    helper.delete();
    tmp.delete();
  }

  public void testStopOnDeleteDir() throws Exception {
    tmp.createDir("a/b");
    manager.start(tmp.get("a/b"));
    manager.start(tmp.get("a"));
    manager.start(tmp.get());

    assertTrue(manager.isStarted(tmp.get("a/b")));
    assertTrue(manager.isStarted(tmp.get("a")));
    assertTrue(manager.isStarted(tmp.get()));

    countDownOnStop(3, new Runnable() {
      @Override public void run() {
        tmp.delete();
      }
    });

    assertFalse(manager.isStarted(tmp.get("a/b")));
    assertFalse(manager.isStarted(tmp.get("a")));
    assertFalse(manager.isStarted(tmp.get()));
  }

  public void testStopOnMoveDir() throws Exception {
    tmp.createDir("a/b");
    manager.start(tmp.get("a/b"));
    manager.start(tmp.get("a"));
    manager.start(tmp.get());

    assertTrue(manager.isStarted(tmp.get("a/b")));
    assertTrue(manager.isStarted(tmp.get("a")));
    assertTrue(manager.isStarted(tmp.get()));

    countDownOnStop(1, new Runnable() {
      @Override public void run() {
        assertTrue(tmp.get().renameTo(helper.get("a")));
      }
    });

    assertFalse(manager.isStarted(tmp.get("a/b")));
    assertFalse(manager.isStarted(tmp.get("a")));
    assertFalse(manager.isStarted(tmp.get()));
  }

  public void testStartOnCreateChildDir() throws Exception {
    manager.start(tmp.get());
    countDownOnStart(1, new Runnable() {
      @Override public void run() {
        tmp.createDir("a");
      }
    });
    assertTrue(manager.isStarted(tmp.get("a")));
  }

  public void testStartOnMoveChildDirIn() throws Exception {
    manager.start(tmp.get());
    countDownOnStart(1, new Runnable() {
      @Override public void run() {
        assertTrue(helper.createDir("a").renameTo(tmp.get("a")));
      }
    });
    assertTrue(manager.isStarted(tmp.get("a")));
  }

  private void countDownOnStop(int count, Runnable runnable)
      throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(count);
    manager.setTestListener(new TestListener() {
      @Override void onStop(DirWatcher observer) {
        latch.countDown();
      }
    });
    runnable.run();
    latch.await(2, SECONDS);
  }

  private void countDownOnStart(int count, Runnable runnable)
      throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(count);
    manager.setTestListener(new TestListener() {
      @Override void onStart(DirWatcher observer) {
        latch.countDown();
      }
    });
    runnable.run();
    latch.await(2, SECONDS);
  }
}
