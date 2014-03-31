package l.files.fse;

import android.os.FileObserver;
import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import l.files.logging.Logger;
import l.files.common.testing.TempDir;

import static android.os.FileObserver.ACCESS;
import static android.os.FileObserver.ATTRIB;
import static android.os.FileObserver.CLOSE_NOWRITE;
import static android.os.FileObserver.CLOSE_WRITE;
import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MODIFY;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static android.os.FileObserver.OPEN;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.append;
import static com.google.common.io.Files.touch;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static l.files.fse.EventServiceTester.FileType.DIR;
import static l.files.fse.EventServiceTester.FileType.FILE;
import static l.files.fse.EventServiceTester.ListenerType.ON_FILE_ADDED;
import static l.files.fse.EventServiceTester.ListenerType.ON_FILE_CHANGED;
import static l.files.fse.EventServiceTester.ListenerType.ON_FILE_REMOVED;
import static org.apache.commons.io.FileUtils.forceDelete;

final class EventServiceTester {

  private static final Logger logger = Logger.get(EventServiceTester.class);

  private final TempDir dir;
  private final FileEventService service;

  private EventServiceTester(FileEventService service, TempDir dir) {
    this.dir = dir;
    this.service = service;
  }

  public static EventServiceTester create(FileEventService service, TempDir dir) {
    return new EventServiceTester(service, dir);
  }

  public TempDir dir() {
    return dir;
  }

  /**
   * Sets the permission of the file located at {@code path} relative to {@link
   * #dir()}, and waits for the notification to be fired.
   */
  public EventServiceTester awaitSetPermission(
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
  public EventServiceTester awaitCreateFile(String path, String monitorPath) {
    return awaitCreate(FILE, dir().get(path), dir().get(monitorPath));
  }

  /**
   * Creates a file at {@code path} relative to {@link #dir()}, and monitors on
   * {@link #dir()} for the file system event.
   */
  public EventServiceTester awaitCreateFile(String path) {
    return awaitCreate(FILE, dir().get(path), dir().get());
  }

  /**
   * Creates a file at {@code path} relative to {@code monitorDir}, and monitors
   * on {@code monitorDir} for the file system event.
   */
  public EventServiceTester awaitCreateFile(String path, File monitorDir) {
    return awaitCreate(FILE, new File(monitorDir, path), monitorDir);
  }

  /**
   * Creates a directory at {@code path} relative to {@link #dir()}, and
   * monitors on {@link #dir()} for the file system event.
   */
  public EventServiceTester awaitCreateDir(String path) {
    return awaitCreate(DIR, dir().get(path), dir().get());
  }

  private EventServiceTester awaitCreate(
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
  public EventServiceTester awaitDelete(String path) {
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
  public EventServiceTester awaitCreateRoot() {
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
  public EventServiceTester awaitDeleteRoot() {
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
  public EventServiceTester awaitMoveFrom(String path, final File dst) {
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
  public EventServiceTester awaitMoveTo(String path, final File src) {
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
  public EventServiceTester awaitMoveRootTo(final File dst) {
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
  public EventServiceTester awaitMoveToRoot(final File src) {
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
  public EventServiceTester awaitModify(String path) {
    final File file = dir().get(path);
    return expect(ON_FILE_CHANGED, MODIFY, file, new Runnable() {
      @Override public void run() {
        try {
          append("0", file, UTF_8);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  private EventServiceTester expect(
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
  private EventServiceTester expect(
      ListenerType type,
      int event,
      File file,
      File monitorDir,
      Runnable runnable) {

    CountDownLatch latch = new CountDownLatch(1);
    TrackingListener listener = type.create(latch, event, file);

    service.register(listener);
    try {
      service.monitor(monitorDir);

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
              "\nExpected: " + getName(event) + "=" + file +
              "\nActual: " + listener.formatted);
        }

        logger.debug("Success waiting for notification." +
            "\nExpected: " + getName(event) + "=" + file +
            "\nActual: " + listener.formatted);

      } catch (InterruptedException e) {
        throw new AssertionError(e);
      }
    } finally {
      service.unregister(listener);
    }

    return this;
  }

  public EventServiceTester monitor() {
    service.monitor(dir().get());
    return this;
  }

  public EventServiceTester monitor(String path) {
    service.monitor(dir().get(path));
    return this;
  }

  public EventServiceTester monitor(File file) {
    service.monitor(file);
    return this;
  }

  private static String getName(int event) {
    switch (event) {
      case ACCESS:
        return "ACCESS";
      case ATTRIB:
        return "ATTRIB";
      case CLOSE_NOWRITE:
        return "CLOSE_NOWRITE";
      case CLOSE_WRITE:
        return "CLOSE_WRITE";
      case CREATE:
        return "CREATE";
      case DELETE:
        return "DELETE";
      case DELETE_SELF:
        return "DELETE_SELF";
      case MODIFY:
        return "MODIFY";
      case MOVE_SELF:
        return "MOVE_SELF";
      case MOVED_FROM:
        return "MOVED_FROM";
      case MOVED_TO:
        return "MOVED_TO";
      case OPEN:
        return "OPEN";
      default:
        throw new IllegalArgumentException("Unknown event: " + event);
    }
  }

  static enum ListenerType {
    ON_FILE_ADDED {
      @Override
      TrackingListener create(CountDownLatch latch, int event, File file) {
        return new CountDownListener(latch, file, event) {
          @Override
          public void onFileAdded(int event, String parent, String path) {
            super.onFileAdded(event, parent, path);
            countDown(event, new File(parent, path));
          }
        };
      }
    },
    ON_FILE_CHANGED {
      @Override
      TrackingListener create(CountDownLatch latch, int event, File file) {
        return new CountDownListener(latch, file, event) {
          @Override
          public void onFileChanged(int event, String parent, String path) {
            super.onFileChanged(event, parent, path);
            countDown(event, new File(parent, path));
          }
        };
      }
    },
    ON_FILE_REMOVED {
      @Override
      TrackingListener create(CountDownLatch latch, int event, File file) {
        return new CountDownListener(latch, file, event) {
          @Override
          public void onFileRemoved(int event, String parent, String path) {
            super.onFileRemoved(event, parent, path);
            countDown(event, new File(parent, path));
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
    final List<File> tracked = synchronizedList(new ArrayList<File>());
    final List<String> formatted = synchronizedList(new ArrayList<String>());

    @Override public void onFileAdded(int event, String parent, String path) {
      track(event, parent, path);
    }

    @Override public void onFileChanged(int event, String parent, String path) {
      track(event, parent, path);
    }

    @Override public void onFileRemoved(int event, String parent, String path) {
      track(event, parent, path);
    }

    private void track(int event, String parent, String path) {
      File file = new File(parent, path);
      tracked.add(file);
      formatted.add(getName(event) + "=" + file);
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

    void countDown(int actualEvent, File actualFile) {
      if (expectedFile.equals(actualFile) &&
          expectedEvent == actualEvent) {
        latch.countDown();
      }
    }
  }
}
