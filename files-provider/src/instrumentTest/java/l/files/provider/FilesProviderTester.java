package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import l.files.common.testing.TempDir;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static l.files.provider.FileCursors.getLastModified;
import static l.files.provider.FileCursors.getLocation;
import static l.files.provider.FileCursors.getSize;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FileCursors.isReadable;
import static l.files.provider.FileCursors.isWritable;
import static l.files.provider.FilesContract.FileInfo.NAME;
import static l.files.provider.FilesContract.buildFileChildrenUri;
import static l.files.provider.FilesContract.getFileLocation;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.comparator.NameFileComparator.NAME_COMPARATOR;

/**
 * Object to help test file system operations.
 * <p/>
 * All {@code await*} methods work by first making a query to get existing
 * children of {@link #dir()}, register a {@link ContentObserver} on the
 * returned cursor, then execute the action specified, wait for the registered
 * {@link ContentObserver} to be notified as the result of the execution event
 * before returning.
 * <p/>
 * All {@code await*} methods will also cause {@link #dir()} to be monitored
 * without calling {@link #monitor()} directly.
 */
final class FilesProviderTester {

  private static final int AWAIT_TIMEOUT = 5;
  private static final TimeUnit AWAIT_TIMEOUT_UNIT = SECONDS;

  private final Context context;
  private final TempDir dir;

  private FilesProviderTester(Context context, TempDir dir) {
    this.context = context;
    this.dir = dir;
  }

  /**
   * @param dir the directory to use, all file operations will be relative to
   * this directory, this can be retrieved via {@link #dir()}.
   */
  public static FilesProviderTester create(Context context, TempDir dir) {
    return new FilesProviderTester(context, dir);
  }

  /**
   * Gets the root directory this instance is operating on.
   */
  public TempDir dir() {
    return dir;
  }

  /**
   * Executes the given runnable and returns {@code this}, allowing chaining.
   */
  public FilesProviderTester run(Runnable runnable) {
    runnable.run();
    return this;
  }

  /**
   * Creates a file at {@code path} relative to {@link #dir()} and waits for the
   * content notification triggered by this action.
   */
  public FilesProviderTester awaitCreateFile(String path) {
    awaitContentChangeClosed(query(), newCreateFile(path));
    return this;
  }

  /**
   * Creates a file at {@code path} relative to {@link #dir()} and waits for the
   * content notification triggered by this action targeting {@code queryPath}.
   */
  public FilesProviderTester awaitCreateFile(String path, String queryPath) {
    File dir = new File(dir().root(), queryPath);
    awaitContentChangeClosed(query(dir), newCreateFile(path));
    return this;
  }

  private Runnable newCreateFile(final String path) {
    return new Runnable() {
      @Override public void run() {
        dir().createFile(path);
      }
    };
  }

  /**
   * Creates a directory with the given path relative to {@link #dir()} and
   * waits for the content notification triggered by this action.
   */
  public FilesProviderTester awaitCreateDir(String path) {
    awaitContentChangeClosed(query(), newCreateDir(path));
    return this;
  }

  private Runnable newCreateDir(final String name) {
    return new Runnable() {
      @Override public void run() {
        dir().createDir(name);
      }
    };
  }

  /**
   * Deletes a file/directory at the given path relative to {@link #dir()} and
   * waits for the content notification triggered by this action.
   */
  public FilesProviderTester awaitDelete(String path) {
    awaitContentChangeClosed(query(), newDelete(new File(dir().root(), path)));
    return this;
  }

  /**
   * Deletes a file/directory at the given path relative to {@link #dir()} and
   * waits for the content notification triggered by this action.
   */
  public FilesProviderTester awaitDeleteRoot() {
    awaitContentChangeClosed(query(), newDelete(dir().root()));
    return this;
  }

  private Runnable newDelete(final File file) {
    return new Runnable() {
      @Override public void run() {
        try {
          forceDelete(file);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  /**
   * Moves a file/directory at the given path relative to {@link #dir()} to
   * {@code dst} and waits for the content notification triggered by this
   * action.
   */
  public FilesProviderTester awaitMoveFrom(String path, File dst) {
    awaitContentChangeClosed(query(), newMove(dir().get(path), dst));
    return this;
  }

  /**
   * Moves {@code src} to the given path relative to {@link #dir()} and waits
   * for the content notification triggered by this action.
   */
  public FilesProviderTester awaitMoveTo(String path, File src) {
    awaitContentChangeClosed(query(), newMove(src, dir().get(path)));
    return this;
  }

  private Runnable newMove(final File from, final File to) {
    return new Runnable() {
      @Override public void run() {
        assertTrue(from.renameTo(to));
      }
    };
  }

  /**
   * Performs a query to ensure {@link #dir()} is monitored by the provider.
   */
  public FilesProviderTester monitor() {
    query();
    return this;
  }

  /**
   * Performs a query to ensure the file/directory at the given path relative to
   * {@link #dir()} is monitored by the provider.
   */
  public FilesProviderTester monitor(String path) {
    query(new File(dir().root(), path));
    return this;
  }

  /**
   * Verifies the data returned by a query to {@link #dir()}, is the same as
   * what's stored on the file system.
   */
  public FilesProviderTester verify() {
    return verify(dir().root());
  }

  /**
   * Verifies the data returned by a query to path relative to {@link #dir()},
   * is the same as what's stored on the file system.
   */
  public FilesProviderTester verify(String path) {
    return verify(new File(dir().root(), path));
  }

  private FilesProviderTester verify(File dir) {
    Cursor cursor = query(dir);
    File[] files = dir.listFiles();
    Arrays.sort(files, NAME_COMPARATOR);

    assertEquals(files.length, cursor.getCount());
    if (cursor.moveToFirst()) {
      do {
        File file = files[cursor.getPosition()];
        assertEquals(file.getName(), FileCursors.getName(cursor));
        assertEquals(getFileLocation(file), getLocation(cursor));
        assertEquals(file.lastModified(), getLastModified(cursor));
        assertEquals(file.length(), getSize(cursor));
        assertEquals(file.isDirectory(), isDirectory(cursor));
        assertEquals(file.canRead(), isReadable(cursor));
        assertEquals(file.canWrite(), isWritable(cursor));
      } while (cursor.moveToNext());
    }

    return this;
  }

  private void awaitContentChangeClosed(Cursor cursor, Runnable code) {
    try {
      awaitContentChange(cursor, code);
    } finally {
      cursor.close();
    }
  }

  private void awaitContentChange(Cursor cursor, Runnable code) {
    WaitForChange change = registerChangeListener(cursor);
    code.run();
    try {
      assertTrue(change.latch.await(AWAIT_TIMEOUT, AWAIT_TIMEOUT_UNIT));
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  private Cursor query() {
    return query(dir().root());
  }

  private Cursor query(File dir) {
    return query(dir, null, null, NAME);
  }

  private Cursor query(
      File dir, String selection, String[] selectionArgs, String order) {
    Uri uri = buildFileChildrenUri(context, getFileLocation(dir));
    ContentResolver resolver = context.getContentResolver();
    return resolver.query(uri, null, selection, selectionArgs, order);
  }

  private WaitForChange registerChangeListener(Cursor cursor) {
    WaitForChange change = new WaitForChange();
    cursor.registerContentObserver(change);
    return change;
  }

  private static class WaitForChange extends ContentObserver {
    final CountDownLatch latch;

    WaitForChange() {
      super(null);
      latch = new CountDownLatch(1);
    }

    @Override public void onChange(boolean selfChange) {
      super.onChange(selfChange);
      latch.countDown();
    }
  }
}
