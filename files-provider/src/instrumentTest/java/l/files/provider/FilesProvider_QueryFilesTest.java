package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import l.files.common.testing.TempDir;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.database.DataTypes.booleanToString;
import static l.files.provider.FileCursors.getLastModified;
import static l.files.provider.FileCursors.getLocation;
import static l.files.provider.FileCursors.getSize;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FileCursors.isReadable;
import static l.files.provider.FileCursors.isWritable;
import static l.files.provider.FilesContract.FileInfo.HIDDEN;
import static l.files.provider.FilesContract.FileInfo.NAME;
import static l.files.provider.FilesContract.buildFileChildrenUri;
import static l.files.provider.FilesContract.getFileLocation;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.write;

public final class FilesProvider_QueryFilesTest extends AndroidTestCase {

  private static final int TIMEOUT = 5;
  private static final TimeUnit TIMEOUT_UNIT = SECONDS;

  private List<Cursor> cursors;
  private TempDir monitored;
  private TempDir helper;

  private Executor originalExecutor;

  public void setUp() throws Exception {
    super.setUp();
    cursors = newArrayList();
    monitored = TempDir.create();
    helper = TempDir.create();
    originalExecutor = FilesDb.executor;
    FilesDb.executor = new SameThreadExecutor();
  }

  public void tearDown() throws Exception {
    FilesDb.executor = originalExecutor;
    for (Cursor cursor : cursors) {
      cursor.close();
    }
    monitored.delete();
    helper.delete();
    super.tearDown();
  }

  public void testUpdateFile() throws Exception {
    File a = monitored.newFile("a");
    writeToFileAndWait(a);
    verify(query(), a);
  }

  /**
   * Directory moved into the monitored directory after querying should be
   * monitored for files additions in that directory, as that will change the
   * new directory's last modified date.
   */
  public void testMoveDirectoryInThenAddFileIntoIt() throws Exception {
    query();
    File dir = moveAndWait(helper.newDirectory(), new File(monitored.get(), "d"));
    createFileAndWait("test", dir);
    verify(query(), dir);
  }

  /**
   * Directory moved into the monitored directory after querying should be
   * monitored for files deletions in that directory, as that will change the
   * new directory's last modified date.
   */
  public void testMoveDirectoryInThenDeleteFileFromIt() throws Exception {
    query();
    File dir = moveAndWait(helper.newDirectory(), new File(monitored.get(), "d"));
    File file = createFileAndWait("test", dir);
    deleteAndWait(file);
    verify(query(), dir);
  }

  /**
   * Directory moved into the monitored directory after querying should be
   * monitored for files moving into that directory, as that will change the new
   * directory's last modified date.
   */
  public void testMoveDirectoryInThenMoveFileIntoIt() throws Exception {
    query();
    File dir = moveAndWait(helper.newDirectory(), new File(monitored.get(), "d"));
    moveAndWait(helper.newFile(), new File(dir, "a"));
    verify(query(), dir);
  }

  /**
   * Directory moved into the monitored directory after querying should be
   * monitored for files moving out of the directory, as that will change the
   * directory's last modified date.
   */
  public void testMoveDirectoryInThenMoveFileOutOfIt() throws Exception {
    query();
    File dir = moveAndWait(helper.newDirectory(), new File(monitored.get(), "d"));
    File file = createFileAndWait("test", dir);
    moveAndWait(file, new File(helper.get(), "a"));
    verify(query(), dir);
  }

  /**
   * Existing directory should be monitored after query for file additions as
   * that will change its last modified date.
   */
  public void testExistingDirectoryAddFileToIt() throws Exception {
    File dir = monitored.newDirectory();
    query();
    createFileAndWait("test", dir);
    verify(query(), dir);
  }

  /**
   * Move a monitored directory out, then changes to the moved directory should
   * no longer be monitored.
   */
  public void testMoveDirectoryOutNoLongerMonitored() throws Exception {
    query();
    File from = monitored.newDirectory("dir");
    File to = moveAndWait(from, new File(helper.get(), "dir"));
    assertFalse(awaitIsChanged(query(), newCreateFile("test", to)));
  }

  /**
   * Move the monitored root directory to somewhere else, then changes to the
   * moved directory should no longer be monitored.
   */
  public void testMoveSelfNoLongerMonitored() throws Exception {
    query();
    File to = new File(helper.get(), "dir");
    assertTrue(monitored.get().renameTo(to));
    assertFalse(awaitIsChanged(query(), newCreateFile("a", to)));
  }

  /**
   * Move the monitored root directory to somewhere else, then the child
   * directories of the root directory should no longer be monitored.
   */
  public void testMoveSelfChildDirectoriesNoLongerMonitored() throws Exception {
    query();
    File oldChild = monitored.newDirectory();
    File newRoot = moveAndWait(monitored.get(), new File(helper.get(), "dir"));
    File newChild = new File(newRoot, oldChild.getName());
    assertTrue(newChild.isDirectory());
    assertFalse(awaitIsChanged(query(), newCreateFile("a", newChild)));
    assertFalse(awaitIsChanged(query(), newCreateFile("b", newChild)));
  }

  /**
   * Move a file out of the directory, changes to the file will no longer be
   * monitored.
   */
  public void testMoveFileOutNoLongerMonitored() throws Exception {
    query();
    File from = monitored.newFile();
    File to = moveAndWait(from, new File(helper.get(), "a"));
    assertFalse(awaitIsChanged(query(), newWrite(to)));
  }

  public void testMoveFileIn() throws Exception {
    verify(query());
    File file = moveAndWait(helper.newFile(), new File(monitored.get(), "a"));
    verify(query(), file);
  }

  public void testMoveFileOut() throws Exception {
    File a = monitored.newFile();
    File b = monitored.newFile();
    verify(query(), a, b);
    moveAndWait(a, new File(helper.get(), "a"));
    verify(query(), b);
  }

  public void testMoveSelfOutAddDirectoryWithSameNameAndMonitor()
      throws Exception {
    query();
    assertTrue(monitored.get().renameTo(new File(helper.get(), "test")));
    assertTrue(monitored.get().mkdirs());
    query();
    File file = createDirectoryAndWait("test");
    verify(query(), file);
  }

  public void testMoveSelfOutMoveDirectoryWithSameNameInAndMonitor()
      throws Exception {
    query();
    assertTrue(monitored.get().renameTo(new File(helper.get(), "test")));
    assertTrue(helper.newDirectory().renameTo(monitored.get()));
    query();
    File file = createDirectoryAndWait("test");
    verify(query(), file);
  }

  private Cursor query() {
    return query(monitored.get());
  }

  private Cursor query(File dir) {
    return query(dir, null, null, NAME);
  }

  private Cursor query(
      File dir, String selection, String[] selectionArgs, String order) {
    Context context = getContext();
    Uri uri = buildFileChildrenUri(context, getFileLocation(dir));
    ContentResolver resolver = context.getContentResolver();
    Cursor cursor = resolver.query(uri, null, selection, selectionArgs, order);
    cursors.add(cursor);
    return cursor;
  }

  private File createDirectoryAndWait(String name) throws Exception {
    return awaitContentChange(query(), newCreateDirectory(name));
  }

  private File createFileAndWait(String name, File parent) throws Exception {
    return createFileAndWait(name, parent, monitored.get());
  }

  private File createFileAndWait(String name, File parent, File monitoredDir)
      throws Exception {
    return awaitContentChange(query(monitoredDir), newCreateFile(name, parent));
  }

  private void deleteAndWait(File file) throws Exception {
    awaitContentChange(query(), newDelete(file));
  }

  private void writeToFileAndWait(File file) throws Exception {
    awaitContentChange(query(), newWrite(file));
  }

  private File moveAndWait(File from, File to) throws Exception {
    return awaitContentChange(query(), newMove(from, to));
  }

  private Callable<File> newCreateDirectory(final String name) {
    return new Callable<File>() {
      @Override public File call() throws Exception {
        return monitored.newDirectory(name);
      }
    };
  }

  private Callable<File> newCreateFile(final String name, final File parent) {
    return new Callable<File>() {
      @Override public File call() throws Exception {
        return TempDir.use(parent).newFile(name);
      }
    };
  }

  private Callable<Void> newDelete(final File file) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        forceDelete(file);
        return null;
      }
    };
  }

  private Callable<Void> newWrite(final File file) {
    return new Callable<Void>() {
      @Override public Void call() throws Exception {
        write(file, String.valueOf(nanoTime()), UTF_8);
        return null;
      }
    };
  }

  private Callable<File> newMove(final File from, final File to) {
    return new Callable<File>() {
      @Override public File call() throws Exception {
        assertTrue(from.renameTo(to));
        return to;
      }
    };
  }

  /**
   * Verifies the cursor contains exactly the given files in the given order,
   * ensures the data in the cursor are up to update by checking against the
   * properties of the files. Since every time a method such as {@link
   * File#lastModified()} is called, the current value of the underlying file
   * will be returned, so calling this method at different point in time will
   * always be checking against the latest of the properties of the files.
   */
  private void verify(Cursor cursor, File... files) {
    assertEquals(files.length, cursor.getCount());
    if (cursor.moveToFirst()) {
      do {
        File file = files[cursor.getPosition()];
        Assert.assertEquals(file.getName(), FileCursors.getName(cursor));
        assertEquals(getFileLocation(file), getLocation(cursor));
        assertEquals(file.lastModified(), getLastModified(cursor));
        assertEquals(file.length(), getSize(cursor));
        assertEquals(file.isDirectory(), isDirectory(cursor));
        assertEquals(file.canRead(), isReadable(cursor));
        assertEquals(file.canWrite(), isWritable(cursor));
      } while (cursor.moveToNext());
    }
  }

  private <V> V awaitContentChange(Cursor cursor, Callable<V> callable)
      throws Exception {
    WaitForChange change = registerChangeListener(cursor);
    V value = callable.call();
    assertTrue(change.latch.await(TIMEOUT, TIMEOUT_UNIT));
    return value;
  }

  private boolean awaitIsChanged(Cursor cursor, Callable<?> callable)
      throws Exception {
    WaitForChange change = registerChangeListener(cursor);
    callable.call();
    return change.latch.await(TIMEOUT, TIMEOUT_UNIT);
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

  private static class SameThreadExecutor implements Executor {
    @Override
    public void execute(@SuppressWarnings("NullableProblems") Runnable cmd) {
      cmd.run();
    }
  }
}
