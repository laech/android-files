package l.files.io.file;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.FileBaseTest;
import l.files.common.testing.TempDir;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.append;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.io.file.WatchService.IGNORED;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

abstract class WatchServiceBaseTest extends FileBaseTest {

  private WatchService service;
  private TempDir helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    service = createService();
    helper = TempDir.create("helper");
  }

  @Override protected void tearDown() throws Exception {
    if (stopServiceOnTearDown()) {
      service.close();
    }
    helper.delete();
    super.tearDown();
  }

  protected File tmpDir() {
    return tmp().get();
  }

  protected WatchService createService() {
    return new WatchServiceImpl(IGNORED);
  }

  protected boolean stopServiceOnTearDown() {
    return true;
  }

  protected final WatchService service() {
    return service;
  }

  protected final TempDir helper() {
    return helper;
  }

  private static abstract class NamedRunnable implements Runnable {
    private final String description;

    NamedRunnable(String description) {
      this.description = description;
    }

    @Override public String toString() {
      return description;
    }
  }

  protected WatchEvent.Listener listen(String relativePath) {
    return listen(tmp().get(relativePath));
  }

  protected WatchEvent.Listener listen(File file) {
    WatchEvent.Listener listener = mock(WatchEvent.Listener.class);
    try {
      service().register(Path.from(file), listener);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return listener;
  }

  protected void unlisten(File file, WatchEvent.Listener listener) {
    service().unregister(Path.from(file), listener);
  }

  protected Runnable newDelete(String relativePath) {
    return newDelete(tmp().get(relativePath));
  }

  protected static Runnable newDelete(final File file) {
    return new NamedRunnable("Delete " + file) {
      @Override public void run() {
        assertTrue(file.exists());
        try {
          forceDelete(file);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    };
  }

  protected Runnable newModify(String relativePath) {
    return newModify(tmp().get(relativePath));
  }

  protected static Runnable newModify(final File file) {
    return new NamedRunnable("Modify " + file) {
      @Override public void run() {
        assertTrue(file.isFile());
        try {
          append("0", file, UTF_8);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    };
  }

  protected Runnable newCreate(String relativePath, FileType type) {
    return newCreate(tmp().get(relativePath), type);
  }

  protected static Runnable newCreate(final File file, final FileType type) {
    return new NamedRunnable("Create " + type + " " + file) {
      @Override public void run() {
        type.create(file);
      }
    };
  }

  protected Runnable newMoveFrom(String relativePath, File dst) {
    return newMove(tmp().get(relativePath), dst);
  }

  protected Runnable newMoveTo(String relativePath, File src) {
    return newMove(src, tmp().get(relativePath));
  }

  protected static Runnable newMove(final File src, final File dst) {
    return new NamedRunnable("Move " + src + " to " + dst) {
      @Override public void run() {
        assertTrue(src.exists());
        assertFalse(dst.exists());
        if (!src.renameTo(dst)) {
          fail("Failed to move " + src + " to " + dst);
        }
      }
    };
  }

  protected Runnable newPermission(
      String relativePath, Permission perm, boolean value) {
    return newPermission(tmp().get(relativePath), perm, value);
  }

  private Runnable newPermission(
      final File file, final Permission perm, final boolean value) {
    return new NamedRunnable("Set " + perm + " to " + value + " for " + file) {
      @Override public void run() {
        perm.set(file, value);
      }
    };
  }

  protected Runnable newLastModified(String relativePath, long value) {
    return newLastModified(tmp().get(relativePath), value);
  }

  private Runnable newLastModified(final File file, final long value) {
    return new NamedRunnable("Set last modified to " + value + " for " + file) {
      @Override public void run() {
        assertTrue(file.exists());
        if (!file.setLastModified(value)) {
          fail("Failed to set last modified " + file);
        }
        assertEquals(value, file.lastModified());
      }
    };
  }

  protected void await(WatchEvent expected, Runnable code) {
    await(expected, code, listen(tmpDir()));
  }

  protected void await(final WatchEvent expected, Runnable code, WatchEvent.Listener listener) {
    final List<WatchEvent> actual = new ArrayList<>();
    final CountDownLatch latch = new CountDownLatch(1);
    doAnswer(new Answer() {
      @Override public Object answer(InvocationOnMock invocation) {
        WatchEvent event = ((WatchEvent) invocation.getArguments()[0]);
        actual.add(event);
        if (expected.equals(event)) {
          latch.countDown();
        }
        return null;
      }
    }).when(listener).onEvent(any(WatchEvent.class));

    code.run();

    try {

      assertTrue("\nExpect: " + expected + "\nActual: " + actual,
          latch.await(1, SECONDS));

    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  protected WatchEvent event(WatchEvent.Kind kind, String relativePath) {
    return WatchEvent.create(kind, Path.from(tmp().get(relativePath)));
  }

  protected WatchEvent event(WatchEvent.Kind kind, File file) {
    return WatchEvent.create(kind, Path.from(file));
  }

  static enum Permission {
    READ {
      @Override boolean get(File file) {
        return file.canRead();
      }

      @Override void set(File file, boolean value) {
        assertTrue(file.setReadable(value, false));
      }
    },
    WRITE {
      @Override boolean get(File file) {
        return file.canWrite();
      }

      @Override void set(File file, boolean value) {
        assertTrue(file.setWritable(value, false));
      }
    };

    abstract boolean get(File file);
    abstract void set(File file, boolean value);
  }

  static enum FileType {
    FILE {
      @Override File create(File f) {
        try {
          File parent = f.getParentFile();
          assertTrue(parent.exists() || parent.mkdirs());
          assertTrue("Failed to create file " + f, !f.exists() && f.createNewFile());
          return f;
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    },
    DIR {
      @Override File create(File f) {
        assertTrue("Failed to create dir " + f, !f.exists() && f.mkdirs());
        return f;
      }
    };

    abstract File create(File file);
  }
}
