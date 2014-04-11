package l.files.fse;

import android.os.FileObserver;
import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.TempDir;
import l.files.io.Path;
import l.files.logging.Logger;

import static android.os.FileObserver.ATTRIB;
import static android.os.FileObserver.CLOSE_WRITE;
import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.append;
import static com.google.common.io.Files.touch;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static l.files.fse.EventObserver.getEventName;
import static l.files.fse.FileEventServiceTester.FileType.DIR;
import static l.files.fse.FileEventServiceTester.FileType.FILE;
import static l.files.fse.FileEventServiceTester.ListenerType.ON_FILE_ADDED;
import static l.files.fse.FileEventServiceTester.ListenerType.ON_FILE_CHANGED;
import static l.files.fse.FileEventServiceTester.ListenerType.ON_FILE_REMOVED;
import static org.apache.commons.io.FileUtils.forceDelete;

final class FileEventServiceTester {

  private static final Logger logger = Logger.get(FileEventServiceTester.class);

  private final TempDir dir;
  private final FileEventService service;

  private FileEventServiceTester(FileEventService service, TempDir dir) {
    this.dir = dir;
    this.service = service;
  }

  public static FileEventServiceTester create(FileEventService service, TempDir dir) {
    return new FileEventServiceTester(service, dir);
  }

  public TempDir dir() {
    return dir;
  }

  public FileEventServiceTester awaitSetFileLastModified(String path, long time) {
    File file = dir().get(path);
    try {
      assertTrue(file.isFile() || file.createNewFile());
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return awaitSetLastModified(file, time);
  }

  public FileEventServiceTester awaitSetDirLastModified(String path, long time) {
    File file = dir().get(path);
    assertTrue(file.isFile() || file.mkdir());
    return awaitSetLastModified(file, time);
  }

  private FileEventServiceTester awaitSetLastModified(
      final File file, final long time) {
    expect(ON_FILE_CHANGED, ATTRIB, file, new Runnable() {
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
  public FileEventServiceTester awaitSetPermission(
      String path, final PermissionType type, final boolean value) {
    final File file = dir().get(path);
    return expect(ON_FILE_CHANGED, ATTRIB, file, new Runnable() {
      @Override public void run() {
        type.set(file, value);
      }
    });
  }

  /**
   * Creates a file at {@code path} relative to {@link #dir()}, and monitors on
   * {@code monitorPath} for the file system event.
   */
  public FileEventServiceTester awaitCreateFile(String path, String monitorPath) {
    return awaitCreate(FILE, dir().get(path), dir().get(monitorPath));
  }

  /**
   * Creates a file at {@code path} relative to {@link #dir()}, and monitors on
   * {@link #dir()} for the file system event.
   */
  public FileEventServiceTester awaitCreateFile(String path) {
    return awaitCreate(FILE, dir().get(path), dir().get());
  }

  /**
   * Creates a file at {@code path} relative to {@code monitorDir}, and monitors
   * on {@code monitorDir} for the file system event.
   */
  public FileEventServiceTester awaitCreateFile(String path, File monitorDir) {
    return awaitCreate(FILE, new File(monitorDir, path), monitorDir);
  }

  /**
   * Creates a directory at {@code path} relative to {@link #dir()}, and
   * monitors on {@link #dir()} for the file system event.
   */
  public FileEventServiceTester awaitCreateDir(String path) {
    return awaitCreate(DIR, dir().get(path), dir().get());
  }

  private FileEventServiceTester awaitCreate(
      final FileType type, final File file, final File monitorDir) {
    return expect(ON_FILE_ADDED, CREATE, file, monitorDir, new Runnable() {
      @Override public void run() {
        type.create(file);
      }
    });
  }

  /**
   * Deletes the file at {@code path} relative to {@link #dir()} and waits for
   * the file system event to be fired.
   */
  public FileEventServiceTester awaitDelete(String path) {
    final File file = dir().get(path);
    return expect(ON_FILE_REMOVED, DELETE, file, new Runnable() {
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
  public FileEventServiceTester awaitCreateRoot() {
    final File dir = dir().get();
    final File parent = dir.getParentFile();
    return expect(ON_FILE_ADDED, CREATE, dir, parent, new Runnable() {
      @Override public void run() {
        assertTrue(parent.exists());
        assertTrue(dir.mkdir());
      }
    });
  }

  /**
   * Deletes {@link #dir()} and waits for the file system event to be fired.
   */
  public FileEventServiceTester awaitDeleteRoot() {
    File file = dir().get();
    return expect(ON_FILE_REMOVED, DELETE_SELF, file, file, new Runnable() {
      @Override public void run() {
        dir().delete();
      }
    });
  }

  /**
   * Moves a file from {@code path} relative to {@link #dir()} to {@code dst},
   * and waits for the file system event to be fired.
   */
  public FileEventServiceTester awaitMoveFrom(String path, final File dst) {
    final File file = dir().get(path);
    return expect(ON_FILE_REMOVED, MOVED_FROM, file, new Runnable() {
      @Override public void run() {
        assertTrue(file.renameTo(dst));
      }
    });
  }

  /**
   * Moves a file to {@code path} relative to {@link #dir()} from {@code src},
   * and waits for the file system event to be fired.
   */
  public FileEventServiceTester awaitMoveTo(String path, final File src) {
    final File file = dir().get(path);
    return expect(ON_FILE_ADDED, MOVED_TO, file, new Runnable() {
      @Override public void run() {
        assertTrue(src.renameTo(file));
      }
    });
  }

  /**
   * Moves {@link #dir()} to {@code dst} and waits for the file system event
   * to.
   */
  public FileEventServiceTester awaitMoveRootTo(final File dst) {
    final File file = dir().get();
    return expect(ON_FILE_REMOVED, MOVE_SELF, file, file, new Runnable() {
      @Override public void run() {
        assertTrue(file.renameTo(dst));
      }
    });
  }

  /**
   * Moves {@code src} to be {@link #dir()} and waits for the file system
   * event.
   */
  public FileEventServiceTester awaitMoveToRoot(final File src) {
    final File dir = dir().get();
    final File parent = dir.getParentFile();
    return expect(ON_FILE_ADDED, MOVED_TO, dir, parent, new Runnable() {
      @Override public void run() {
        assertTrue(src.renameTo(dir));
      }
    });
  }

  /**
   * Modifies a file located at {@code path} relative to {@link #dir()} and
   * waits for the file system event.
   */
  public FileEventServiceTester awaitModify(String path) {
    final File file = dir().get(path);
    return expect(ON_FILE_CHANGED, CLOSE_WRITE, file, new Runnable() {
      @Override public void run() {
        try {
          append("0", file, UTF_8);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  private FileEventServiceTester expect(
      ListenerType type, int event, File file, Runnable runnable) {
    return expect(type, event, file, dir().get(), runnable);
  }

  /**
   * Expects an event to happen.
   *
   * @param type the type of listener to create for tracking the event
   * @param event the expected type of {@link FileObserver} event
   * @param file the expected file of the event
   * @param monitorDir the parent directory to monitor for the event
   * @param runnable the code to trigger the event
   */
  private FileEventServiceTester expect(
      ListenerType type,
      int event,
      File file,
      File monitorDir,
      Runnable runnable) {

    CountDownLatch latch = new CountDownLatch(1);
    TrackingListener listener = type.create(latch, event, file);

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
              "\nExpected: " + getEventName(event) + "=" + file +
              "\nActual: " + listener.formatted);
        }

        logger.debug("Success waiting for notification." +
            "\nExpected: " + getEventName(event) + "=" + file +
            "\nActual: " + listener.formatted);

      } catch (InterruptedException e) {
        throw new AssertionError(e);
      }
    } finally {
      service.unregister(listener);
    }

    return this;
  }

  public FileEventServiceTester monitor() {
    service.monitor(Path.from(dir().get()));
    return this;
  }

  public FileEventServiceTester unmonitor() {
    service.unmonitor(Path.from(dir().get()));
    return this;
  }

  public FileEventServiceTester monitor(String path) {
    service.monitor(Path.from(dir().get(path)));
    return this;
  }

  public FileEventServiceTester monitor(File file) {
    service.monitor(Path.from(file));
    return this;
  }

  static enum ListenerType {
    ON_FILE_ADDED {
      @Override
      TrackingListener create(CountDownLatch latch, int event, File file) {
        return new CountDownListener(latch, file, event) {
          @Override public void onFileAdded(int event, Path path) {
            super.onFileAdded(event, path);
            countDown(event, path);
          }
        };
      }
    },
    ON_FILE_CHANGED {
      @Override
      TrackingListener create(CountDownLatch latch, int event, File file) {
        return new CountDownListener(latch, file, event) {
          @Override public void onFileChanged(int event, Path path) {
            super.onFileChanged(event, path);
            countDown(event, path);
          }
        };
      }
    },
    ON_FILE_REMOVED {
      @Override
      TrackingListener create(CountDownLatch latch, int event, File file) {
        return new CountDownListener(latch, file, event) {
          @Override public void onFileRemoved(int event, Path path) {
            super.onFileRemoved(event, path);
            countDown(event, path);
          }
        };
      }
    };

    abstract TrackingListener create(CountDownLatch latch, int event, File file);
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

  static class TrackingListener implements FileEventListener {
    final List<Path> tracked = synchronizedList(new ArrayList<Path>());
    final List<String> formatted = synchronizedList(new ArrayList<String>());

    @Override public void onFileAdded(int event, Path path) {
      track(event, path);
    }

    @Override public void onFileChanged(int event, Path path) {
      track(event, path);
    }

    @Override public void onFileRemoved(int event, Path path) {
      track(event, path);
    }

    private void track(int event, Path path) {
      tracked.add(path);
      formatted.add(getEventName(event) + "=" + path);
    }
  }

  static class CountDownListener extends TrackingListener {
    private final CountDownLatch latch;
    private final File expectedFile;
    private final int expectedEvent;

    CountDownListener(CountDownLatch latch, File expectedFile, int expectedEvent) {
      this.latch = latch;
      this.expectedFile = expectedFile;
      this.expectedEvent = expectedEvent;
    }

    void countDown(int actualEvent, Path actualPath) {
      if (expectedFile.equals(actualPath.toFile()) &&
          0 != (expectedEvent & actualEvent)) {
        latch.countDown();
      }
    }
  }
}
