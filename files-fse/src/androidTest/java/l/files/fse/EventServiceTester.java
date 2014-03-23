package l.files.fse;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.TempDir;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.append;
import static com.google.common.io.Files.touch;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;
import static l.files.fse.EventServiceTester.ExpectedEvent.ADDED;
import static l.files.fse.EventServiceTester.ExpectedEvent.CHANGED;
import static l.files.fse.EventServiceTester.ExpectedEvent.REMOVED;
import static l.files.fse.EventServiceTester.FileType.DIR;
import static l.files.fse.EventServiceTester.FileType.FILE;
import static org.apache.commons.io.FileUtils.forceDelete;

final class EventServiceTester {

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
    return expect(CHANGED, file, new Runnable() {
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
    return expect(ADDED, file, monitorDir, new Runnable() {
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
    return expect(REMOVED, file, new Runnable() {
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
   * Deletes {@link #dir()} and waits for the file system event to be fired.
   */
  public EventServiceTester awaitDeleteRoot() {
    File file = dir().get();
    return expect(REMOVED, file, file, new Runnable() {
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
    return expect(REMOVED, file, new Runnable() {
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
    return expect(ADDED, file, new Runnable() {
      @Override public void run() {
        assertTrue(src.renameTo(file));
      }
    });
  }

  /**
   * Moves {@link #dir()} to {@code dst} and waits for the file system event to
   * be fired.
   */
  public EventServiceTester awaitMoveRootTo(final File dst) {
    final File file = dir().get();
    return expect(REMOVED, file, file, new Runnable() {
      @Override public void run() {
        assertTrue(file.renameTo(dst));
      }
    });
  }

  /**
   * Modifies a file located at {@code path} relative to {@link #dir()} and
   * waits for the file system event.
   */
  public EventServiceTester awaitModify(String path) {
    final File file = dir().get(path);
    return expect(CHANGED, file, new Runnable() {
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
      ExpectedEvent type, File file, Runnable runnable) {
    return expect(type, file, dir().get(), runnable);
  }

  private EventServiceTester expect(
      ExpectedEvent type, File file, File monitorDir, Runnable runnable) {
    CountDownLatch latch = new CountDownLatch(1);
    FileEventListener listener = type.create(file, latch);

    service.register(listener);
    try {
      service.monitor(monitorDir);
      runnable.run();

      try {

        assertTrue("file=" + file
                + ", monitorDir=" + monitorDir
                + ", type=" + type,
            latch.await(5, SECONDS)
        );

      } catch (InterruptedException e) {
        throw new AssertionError(e);
      }
    } finally {
      service.unregister(listener);
    }

    return this;
  }

  public EventServiceTester run(Runnable code) {
    code.run();
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

  static enum ExpectedEvent {
    ADDED {
      @Override FileEventListener create(final File file, final CountDownLatch latch) {
        return new FileEventAdapter() {
          @Override public void onFileAdded(String parent, String path) {
            countDown(latch, file, new File(parent, path));
          }
        };
      }
    },
    CHANGED {
      @Override FileEventListener create(final File file, final CountDownLatch latch) {
        return new FileEventAdapter() {
          @Override public void onFileChanged(String parent, String path) {
            countDown(latch, file, new File(parent, path));
          }
        };
      }
    },
    REMOVED {
      @Override FileEventListener create(final File file, final CountDownLatch latch) {
        return new FileEventAdapter() {
          @Override public void onFileRemoved(String parent, String path) {
            countDown(latch, file, new File(parent, path));
          }
        };
      }
    };

    void countDown(CountDownLatch latch, File expected, File actual) {
      if (expected.equals(actual)) {
        latch.countDown();
      }
    }

    abstract FileEventListener create(File file, CountDownLatch latch);
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
}
