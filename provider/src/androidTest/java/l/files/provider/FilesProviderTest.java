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
import l.files.io.file.FileInfo;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.io.Files.write;
import static com.google.common.truth.Truth.ASSERT;
import static java.util.Arrays.sort;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.io.file.Files.symlink;
import static l.files.provider.FileCursors.getId;
import static l.files.provider.FileCursors.getLastModified;
import static l.files.provider.FileCursors.getSize;
import static l.files.provider.FileCursors.getType;
import static l.files.provider.FileCursors.isDirectory;
import static l.files.provider.FileCursors.isReadable;
import static l.files.provider.FileCursors.isWritable;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_SIZE;
import static l.files.provider.FilesContract.FileInfo.TYPE_DIRECTORY;
import static l.files.provider.FilesContract.FileInfo.TYPE_REGULAR_FILE;
import static l.files.provider.FilesContract.FileInfo.TYPE_SYMLINK;
import static l.files.provider.FilesContract.FileInfo.TYPE_UNKNOWN;
import static l.files.provider.FilesContract.buildFileChildrenUri;
import static l.files.provider.FilesContract.buildSelectionUri;
import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;
import static org.apache.commons.io.comparator.NameFileComparator.NAME_COMPARATOR;
import static org.apache.commons.io.comparator.SizeFileComparator.SIZE_REVERSE;

public final class FilesProviderTest extends FileBaseTest {

  public void testQuerySymlink() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().get("b");
    symlink(a.getPath(), b.getPath());
    verify();
  }

  public void testQueryFile() throws Exception {
    tmp().createFile("a");
    verify();
  }

  public void testQueryDir() throws Exception {
    tmp().createDir("b");
    verify();
  }

  public void testQueryMultiple() throws Exception {
    tmp().createFile("a");
    tmp().createDir("b");
    tmp().createFile("c");
    tmp().createFile("d");
    verify();
  }

  public void testQueryHiddenFiles() throws Exception {
    tmp().createFile("a");
    tmp().createFile(".b");
    tmp().createDir(".c");
    tmp().createDir("d");
    verify(false, "a", "d");
  }

  public void testQueryMultipleDirs() throws Exception {
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

  public void testQueryExistingContent() throws Exception {
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

  public void testQuerySelection() throws Exception {
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

  public void testQuerySortByName() throws Exception {
    tmp().createFile("z");
    tmp().createDir("x");
    tmp().createFile("y");
    verify(tmp().get(), SORT_BY_NAME, NAME_COMPARATOR);
  }

  public void testQuerySortByDate() throws Exception {
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

  public void testNotifiesOnFileAdditionDeletion() throws Exception {
    testNotifies(new Runnable() {
      @Override public void run() {
        File dir = tmp().createDir("a");
        assertTrue(dir.delete());
        assertTrue(dir.mkdirs());
      }
    });
  }

  private void testNotifies(Runnable code)
      throws InterruptedException, IOException {
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

  private void verify() throws IOException {
    verify(tmp().get());
  }

  private void verify(String path) throws IOException {
    verify(tmp().get(path));
  }

  private void verify(boolean showHidden, String... paths) throws IOException {
    File[] files = new File[paths.length];
    for (int i = 0; i < files.length; i++) {
      files[i] = tmp().get(paths[i]);
    }
    verify(tmp().get(), SORT_BY_NAME, NAME_COMPARATOR, showHidden, files);
  }

  private void verify(File dir, String order, Comparator<File> comparator)
      throws IOException {
    verify(dir, order, comparator, true, dir.listFiles());
  }

  private void verify(
      File dir,
      String order,
      Comparator<File> comparator,
      boolean showHidden,
      File... files) throws IOException {
    Cursor cursor = queryChildren(dir, showHidden, order);
    try {
      sort(files, comparator);
      verify(cursor, files);
    } finally {
      cursor.close();
    }
  }

  private void verify(File dir) throws IOException {
    verify(dir, SORT_BY_NAME, NAME_COMPARATOR, true, dir.listFiles());
  }

  private static void verify(Cursor cursor, File[] files) throws IOException {
    List<String> expected = getIds(files);
    List<String> actual = getIds(cursor);
    ASSERT.that(actual).is(expected);
    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      File file = files[cursor.getPosition()];
      FileInfo info = FileInfo.get(file.getPath());
      ASSERT.that(FileCursors.getName(cursor)).is(info.getName());
      ASSERT.that(getId(cursor)).is(FilesContract.getFileId(file));
      ASSERT.that(getLastModified(cursor)).is(info.getLastModified());
      ASSERT.that(getSize(cursor)).is(info.getSize());
      ASSERT.that(isDirectory(cursor)).is(info.isDirectory());
      ASSERT.that(isReadable(cursor)).is(info.isReadable());
      ASSERT.that(isWritable(cursor)).is(info.isWritable());

      if (info.isRegularFile()) {
        ASSERT.that(getType(cursor)).is(TYPE_REGULAR_FILE);
      } else if (info.isSymbolicLink()) {
        ASSERT.that(getType(cursor)).is(TYPE_SYMLINK);
      } else if (info.isDirectory()) {
        ASSERT.that(getType(cursor)).is(TYPE_DIRECTORY);
      } else {
        ASSERT.that(getType(cursor)).is(TYPE_UNKNOWN);
      }
    }
  }

  private static List<String> getIds(Cursor cursor) {
    List<String> names = newArrayListWithCapacity(cursor.getCount());
    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      names.add(getId(cursor));
    }
    return names;
  }

  private static List<String> getIds(File[] files) {
    List<String> names = newArrayListWithCapacity(files.length);
    for (File file : files) {
      names.add(FilesContract.getFileId(file));
    }
    return names;
  }

  private Cursor queryChildren() {
    return queryChildren(tmp().get(), true, SORT_BY_NAME);
  }

  private Cursor queryChildren(File dir, boolean showHidden, String order) {
    Context context = getContext();
    Uri uri = buildFileChildrenUri(context, FilesContract.getFileId(dir), showHidden);
    ContentResolver resolver = context.getContentResolver();
    return resolver.query(uri, null, null, null, order);
  }

  private Cursor querySelection(File... files) {
    String[] ids = new String[files.length];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = FilesContract.getFileId(files[i]);
    }
    Context context = getContext();
    ContentResolver resolver = context.getContentResolver();
    Uri uri = buildSelectionUri(context, ids);
    return resolver.query(uri, null, null, null, null);
  }
}
