package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.FileBaseTest;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.io.Files.write;
import static java.util.Arrays.sort;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.provider.FileCursors.getLastModified;
import static l.files.provider.FileCursors.getLocation;
import static l.files.provider.FileCursors.getSize;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FileCursors.isReadable;
import static l.files.provider.FileCursors.isWritable;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_SIZE;
import static l.files.provider.FilesContract.buildFileChildrenUri;
import static l.files.provider.FilesContract.buildSelectionUri;
import static l.files.provider.FilesContract.getFileLocation;
import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;
import static org.apache.commons.io.comparator.NameFileComparator.NAME_COMPARATOR;
import static org.apache.commons.io.comparator.SizeFileComparator.SIZE_REVERSE;

public final class FilesProviderTest extends FileBaseTest {

  public void testQueryFile() {
    tmp().createFile("a");
    verify();
  }

  public void testQueryDir() {
    tmp().createDir("b");
    verify();
  }

  public void testQueryMultiple() {
    tmp().createFile("a");
    tmp().createDir("b");
    tmp().createFile("c");
    tmp().createFile("d");
    verify();
  }

  public void testQueryHiddenFiles() {
    tmp().createFile("a");
    tmp().createFile(".b");
    tmp().createDir(".c");
    tmp().createDir("d");
    verify(false, "a", "d");
  }

  public void testQueryMultipleDirs() {
    tmp().createDir("a");
    tmp().createFile("b");
    tmp().createDir("c/a");
    tmp().createFile("c/b");
    tmp().createFile("c/c");
    tmp().createFile("d/a");
    tmp().createFile("d/b");

    verify();
    verify("c");
    verify("d");
  }

  public void testQueryExistingContent() {
    tmp().createFile("a");
    tmp().createFile("b");
    tmp().createFile("c/a");
    tmp().createFile("c/b");
    tmp().createFile("d/a");
    tmp().createFile("d/b");
    tmp().createFile("d/c");

    verify();
    verify("c");
    verify("d");
  }

  public void testQuerySelection() {
    File[] files = {
        new File("/"),
        tmp().get(),
        tmp().createFile("a")
    };
    Cursor cursor = querySelection(files);
    try {
      verify(cursor, files);
    } finally {
      cursor.close();
    }
  }

  public void testQuerySortByName() {
    tmp().createFile("z");
    tmp().createDir("x");
    tmp().createFile("y");
    verify(tmp().get(), SORT_BY_NAME, NAME_COMPARATOR);
  }

  public void testQuerySortByDate() {
    assertTrue(tmp().createFile("z").setLastModified(1000));
    assertTrue(tmp().createDir("x").setLastModified(2000));
    assertTrue(tmp().createFile("y").setLastModified(3000));
    verify(tmp().get(), SORT_BY_MODIFIED, LASTMODIFIED_REVERSE);
  }

  public void testQuerySortBySize() throws Exception {
    write("x", tmp().createFile("z"), UTF_8);
    write("xx", tmp().createFile("x"), UTF_8);
    write("xxx", tmp().createFile("y"), UTF_8);
    verify(tmp().get(), SORT_BY_SIZE, SIZE_REVERSE);
  }

  public void testNotifiesOnFileAddition() throws Exception {
    testNotifies(new Runnable() {
      @Override public void run() {
        tmp().createFile("a");
      }
    });
  }

  public void testNotifiesOnFileDeletion() throws Exception {
    final File file = tmp().createFile("a");
    testNotifies(new Runnable() {
      @Override public void run() {
        assertTrue(file.delete());
      }
    });
  }

  public void testNotifiesOnFileModification() throws Exception {
    final File file = tmp().createFile("a");
    testNotifies(new Runnable() {
      @Override public void run() {
        try {
          write("x", file, UTF_8);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  private void testNotifies(Runnable code) throws InterruptedException {
    Cursor cursor = queryChildren();
    try {

      final CountDownLatch latch = new CountDownLatch(1);
      cursor.registerContentObserver(new ContentObserver(null) {
        @Override public void onChange(boolean selfChange) {
          super.onChange(selfChange);
          latch.countDown();
        }
      });

      code.run();

      assertTrue(latch.await(1, SECONDS));
      verify();

    } finally {
      cursor.close();
    }
  }

  private void verify() {
    verify(tmp().get());
  }

  private void verify(String path) {
    verify(tmp().get(path));
  }

  private void verify(boolean showHidden, String... paths) {
    File[] files = new File[paths.length];
    for (int i = 0; i < files.length; i++) {
      files[i] = tmp().get(paths[i]);
    }
    verify(tmp().get(), SORT_BY_NAME, NAME_COMPARATOR, showHidden, files);
  }

  private void verify(File dir, String order, Comparator<File> comparator) {
    verify(dir, order, comparator, true, dir.listFiles());
  }

  private void verify(
      File dir,
      String order,
      Comparator<File> comparator,
      boolean showHidden,
      File... files) {
    Cursor cursor = queryChildren(dir, showHidden, order);
    try {
      sort(files, comparator);
      verify(cursor, files);
    } finally {
      cursor.close();
    }
  }

  private void verify(File dir) {
    verify(dir, SORT_BY_NAME, NAME_COMPARATOR, true, dir.listFiles());
  }

  private static void verify(Cursor cursor, File[] files) {
    List<String> expected = getLocations(files);
    List<String> actual = getLocations(cursor);
    assertEquals(expected, actual);
    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      File file = files[cursor.getPosition()];
      assertEquals(file.getName(), FileCursors.getName(cursor));
      assertEquals(getFileLocation(file), getLocation(cursor));
      assertEquals(file.lastModified(), getLastModified(cursor));
      assertEquals(file.length(), getSize(cursor));
      assertEquals(file.isDirectory(), isDirectory(cursor));
      assertEquals(file.canRead(), isReadable(cursor));
      assertEquals(file.canWrite(), isWritable(cursor));
    }
  }

  private static List<String> getLocations(Cursor cursor) {
    List<String> names = newArrayListWithCapacity(cursor.getCount());
    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      names.add(getLocation(cursor));
    }
    return names;
  }

  private static List<String> getLocations(File[] files) {
    List<String> names = newArrayListWithCapacity(files.length);
    for (File file : files) {
      names.add(getFileLocation(file));
    }
    return names;
  }

  private Cursor queryChildren() {
    return queryChildren(tmp().get(), true, SORT_BY_NAME);
  }

  private Cursor queryChildren(File dir, boolean showHidden, String order) {
    Context context = getContext();
    Uri uri = buildFileChildrenUri(context, getFileLocation(dir), showHidden);
    ContentResolver resolver = context.getContentResolver();
    return resolver.query(uri, null, null, null, order);
  }

  private Cursor querySelection(File... files) {
    String[] locations = new String[files.length];
    for (int i = 0; i < locations.length; i++) {
      locations[i] = getFileLocation(files[i]);
    }
    Context context = getContext();
    ContentResolver resolver = context.getContentResolver();
    Uri uri = buildSelectionUri(context, locations);
    return resolver.query(uri, null, null, null, null);
  }
}
