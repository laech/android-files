package l.files.fse;

import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.TempDir;
import l.files.io.Path;
import l.files.logging.Logger;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.append;
import static com.google.common.io.Files.touch;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static l.files.fse.WatchServiceTester.FileType.DIR;
import static l.files.fse.WatchServiceTester.FileType.FILE;
import static l.files.fse.WatchEvent.Kind.CREATE;
import static l.files.fse.WatchEvent.Kind.DELETE;
import static l.files.fse.WatchEvent.Kind.MODIFY;
import static org.apache.commons.io.FileUtils.forceDelete;

final class WatchServiceTester {

  private static final Logger logger = Logger.get(WatchServiceTester.class);

  private final TempDir dir;
  private final WatchService service;

  private WatchServiceTester(WatchService service, TempDir dir) {
    this.dir = dir;
    this.service = service;
  }

  public static WatchServiceTester create(WatchService service, TempDir dir) {
    return new WatchServiceTester(service, dir);
  }

  public TempDir dir() {
    return dir;
  }

  public WatchServiceTester awaitSetFileLastModified(String path, long time) {
    File file = dir().get(path);
    try {
      assertTrue(file.isFile() || file.createNewFile());
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return awaitSetLastModified(file, time);
  }

  public WatchServiceTester awaitSetDirLastModified(String path, long time) {
    File file = dir().get(path);
    assertTrue(file.isFile() || file.mkdir());
    return awaitSetLastModified(file, time);
  }

  private WatchServiceTester awaitSetLastModified(
      final File file, final long time) {
    expect(MODIFY, file, new Runnable() {
      @Override public void run() {
        assertTrue(file.setLastModified(time));
      }
    });
    return this;
  }

  /**
   * Sets the permission of the file located at {@code path} relative to {@link
   * #dir()}, and waits for the notification to be fired.
   */
  public WatchServiceTester awaitSetPermission(
      String path, final PermissionType type, final boolean value) {
    final File file = dir().get(path);
    return expect(MODIFY, file, new Runnable() {
      @Override public void run() {
        type.set(file, value);
      }
    });
  }

  /**
   * Creates a file at {@code path} relative to {@link #dir()}, and monitors on
   * {@code monitorPath} for the file system event.
   */
  public WatchServiceTester awaitCreateFile(String path, String monitorPath) {
    return awaitCreate(FILE, dir().get(path), dir().get(monitorPath));
  }

  /**
   * Creates a file at {@code path} relative to {@link #dir()}, and monitors on
   * {@link #dir()} for the file system event.
   */
  public WatchServiceTester awaitCreateFile(String path) {
    return awaitCreate(FILE, dir().get(path), dir().get());
  }

  /**
   * Creates a file at {@code path} relative to {@code monitorDir}, and monitors
   * on {@code monitorDir} for the file system event.
   */
  public WatchServiceTester awaitCreateFile(String path, File monitorDir) {
    return awaitCreate(FILE, new File(monitorDir, path), monitorDir);
  }

  /**
   * Creates a directory at {@code path} relative to {@link #dir()}, and
   * monitors on {@link #dir()} for the file system event.
   */
  public WatchServiceTester awaitCreateDir(String path) {
    return awaitCreate(DIR, dir().get(path), dir().get());
  }

  private WatchServiceTester awaitCreate(
      final FileType type, final File file, final File monitorDir) {
    return expect(CREATE, file, monitorDir, new Runnable() {
      @Override public void run() {
        type.create(file);
      }
    });
  }

  /**
   * Deletes the file at {@code path} relative to {@link #dir()} and waits for
   * the file system event to be fired.
   */
  public WatchServiceTester awaitDelete(String path) {
    final File file = dir().get(path);
    return expect(DELETE, file, new Runnable() {
      @Override public void run() {
        assertTrue(file.exists());
        try {
          forceDelete(file);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  /**
   * Creates {@link #dir()} and waits for the file system event.
   */
  public WatchServiceTester awaitCreateRoot() {
    final File dir = dir().get();
    final File parent = dir.getParentFile();
    return expect(CREATE, dir, parent, new Runnable() {
      @Override public void run() {
        assertTrue(parent.exists());
        assertTrue(dir.mkdir());
      }
    });
  }

  /**
   * Deletes {@link #dir()} and waits for the file system event to be fired.
   */
  public WatchServiceTester awaitDeleteRoot() {
    File file = dir().get();
    return expect(DELETE, file, file, new Runnable() {
      @Override public void run() {
        dir().delete();
      }
    });
  }

  /**
   * Moves a file from {@code path} relative to {@link #dir()} to {@code dst},
   * and waits for the file system event to be fired.
   */
  public WatchServiceTester awaitMoveFrom(String path, final File dst) {
    final File file = dir().get(path);
    return expect(DELETE, file, new Runnable() {
      @Override public void run() {
        assertTrue(file.renameTo(dst));
      }
    });
  }

  /**
   * Moves a file to {@code path} relative to {@link #dir()} from {@code src},
   * and waits for the file system event to be fired.
   */
  public WatchServiceTester awaitMoveTo(String path, final File src) {
    final File file = dir().get(path);
    return expect(CREATE, file, new Runnable() {
      @Override public void run() {
        assertTrue(src.renameTo(file));
      }
    });
  }

  /**
   * Moves {@link #dir()} to {@code dst} and waits for the file system event
   * to.
   */
  public WatchServiceTester awaitMoveRootTo(final File dst) {
    final File file = dir().get();
    return expect(DELETE, file, file, new Runnable() {
      @Override public void run() {
        assertTrue(file.renameTo(dst));
      }
    });
  }

  /**
   * Moves {@code src} to be {@link #dir()} and waits for the file system
   * event.
   */
  public WatchServiceTester awaitMoveToRoot(final File src) {
    final File dir = dir().get();
    final File parent = dir.getParentFile();
    return expect(CREATE, dir, parent, new Runnable() {
      @Override public void run() {
        assertTrue(src.renameTo(dir));
      }
    });
  }

  /**
   * Modifies a file located at {@code path} relative to {@link #dir()} and
   * waits for the file system event.
   */
  public WatchServiceTester awaitModify(String path) {
    final File file = dir().get(path);
    return expect(MODIFY, file, new Runnable() {
      @Override public void run() {
        try {
          append("0", file, UTF_8);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  private WatchServiceTester expect(
      WatchEvent.Kind kind, File file, Runnable runnable) {
    return expect(kind, file, dir().get(), runnable);
  }

  /**
   * Expects an event to happen.
   *
   * @param kind the expected event kind
   * @param file the expected file of the event
   * @param monitorDir the parent directory to monitor for the event
   * @param runnable the code to trigger the event
   */
  private WatchServiceTester expect(
      WatchEvent.Kind kind,
      File file,
      File monitorDir,
      Runnable runnable) {

    CountDownLatch latch = new CountDownLatch(1);
    WatchEvent expected = WatchEvent.create(kind, Path.from(file));
    TrackingListener listener = new CountDownListener(latch, expected);

    service.register(listener);
    try {
      service.monitor(Path.from(monitorDir));

      /*
        If monitoring an entire directory subtree, and a new subdirectory is
        created in that tree, by the time we create a watch for the
        new subdirectory, new files may already have been created in the
        subdirectory, so the creation of the new files won't be tracked.
        Therefore put a sleep here to allow the watch to be added first.

        Also documented on the service that caller cannot rely on the service
        for this reason.
       */
      SystemClock.sleep(2);

      runnable.run();

      try {

        if (!latch.await(2, SECONDS)) {
          fail("Timed out waiting for notification. " + runnable.toString() +
              "\nExpected: " + kind + "=" + file +
              "\nActual: " + listener.tracked);
        }

        logger.debug("Success waiting for notification." +
            "\nExpected: " + kind + "=" + file +
            "\nActual: " + listener.tracked);

      } catch (InterruptedException e) {
        throw new AssertionError(e);
      }
    } finally {
      service.unregister(listener);
    }

    return this;
  }

  public WatchServiceTester monitor() {
    service.monitor(Path.from(dir().get()));
    return this;
  }

  public WatchServiceTester unmonitor() {
    service.unmonitor(Path.from(dir().get()));
    return this;
  }

  public WatchServiceTester monitor(String path) {
    service.monitor(Path.from(dir().get(path)));
    return this;
  }

  public WatchServiceTester monitor(File file) {
    service.monitor(Path.from(file));
    return this;
  }

  static enum FileType {
    FILE {
      @Override void create(File file) {
        try {
          File parent = file.getParentFile();
          assertTrue(parent.exists() || parent.mkdirs());
          touch(file);
        } catch (IOException e) {
          throw new RuntimeException("Failed to create " + file, e);
        }
      }
    },
    DIR {
      @Override void create(File file) {
        assertTrue(file.mkdirs());
      }
    };

    abstract void create(File file);
  }

  static enum PermissionType {
    READ {
      @Override void set(File file, boolean value) {
        assertTrue(file.setReadable(value, false));
      }
    },
    WRITE {
      @Override void set(File file, boolean value) {
        assertTrue(file.setWritable(value, false));
      }
    };

    /**
     * Sets the permission value to the given file.
     */
    abstract void set(File file, boolean value);
  }

  static class TrackingListener implements WatchEvent.Listener {
    final List<WatchEvent> tracked = synchronizedList(new ArrayList<WatchEvent>());

    @Override public void onEvent(WatchEvent event) {
      tracked.add(event);
    }
  }

  static class CountDownListener extends TrackingListener {
    private final CountDownLatch latch;
    private final WatchEvent expected;

    CountDownListener(CountDownLatch latch, WatchEvent expected) {
      this.latch = latch;
      this.expected = expected;
    }

    @Override public void onEvent(WatchEvent event) {
      super.onEvent(event);
      if (event.equals(expected)) {
        latch.countDown();
      }
    }
  }
}
