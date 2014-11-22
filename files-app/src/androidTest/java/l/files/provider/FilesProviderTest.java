package l.files.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import l.files.common.testing.FileBaseTest;
import l.files.fs.local.LocalFileStatus;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.io.Files.write;
import static java.util.Arrays.sort;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.assertExists;
import static l.files.fs.local.Files.symlink;
import static l.files.provider.FilesContract.Files;
import static l.files.provider.FilesContract.Files.SORT_BY_MODIFIED;
import static l.files.provider.FilesContract.Files.SORT_BY_NAME;
import static l.files.provider.FilesContract.Files.SORT_BY_SIZE;
import static l.files.provider.FilesContract.Files.TYPE_DIRECTORY;
import static l.files.provider.FilesContract.Files.TYPE_REGULAR_FILE;
import static l.files.provider.FilesContract.Files.TYPE_SYMLINK;
import static l.files.provider.FilesContract.Files.TYPE_UNKNOWN;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.FilesContract.getFileUri;
import static l.files.provider.FilesContract.getFilesUri;
import static l.files.provider.FilesContract.getSelectionUri;
import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;
import static org.apache.commons.io.comparator.NameFileComparator.NAME_COMPARATOR;
import static org.apache.commons.io.comparator.SizeFileComparator.SIZE_REVERSE;

public final class FilesProviderTest extends FileBaseTest {

  public void testDetectsMediaTypeForFile() throws Exception {
    File file = tmp().createFile("a.mp3");
    write("hello world", file, UTF_8);
    assertEquals("text/plain", getMediaType(file));
  }

  public void testDetectsMediaTypeForDirectory() throws Exception {
    File dir = tmp().createDir("a.mp3");
    assertEquals("inode/directory", getMediaType(dir));
  }

  public void testReturnUnknownMediaTypeForUnreadableFile() throws Exception {
    File file = tmp().createFile("a.txt");
    write("hello world", file, UTF_8);
    assertTrue(file.setReadable(false));
    assertEquals("application/octet-stream", getMediaType(file));
  }

  public void testReturnUnknownMediaTypeForSystemFile() throws Exception {
    File file = new File("/proc/1/maps");
    assertExists(file);
    assertEquals("application/octet-stream", getMediaType(file));
  }

  private String getMediaType(File file) {
    Uri uri = getFileUri(getContext(), getFileId(file));
    return getContext().getContentResolver().getType(uri);
  }

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
    try (Cursor cursor = querySelection(files)) {
      verify(cursor, files);
    }
  }

  public void testQuerySortByName() throws Exception {
    tmp().createFile("z");
    tmp().createDir("x");
    tmp().createFile("y");
    verify(tmp().get(), SORT_BY_NAME, NAME_COMPARATOR);
  }

  public void testQuerySortByNameLocaleSensitive() throws Exception {
    class NameCollator implements Comparator<File> {
      private final Collator collator;

      NameCollator(Collator collator) {
        this.collator = collator;
      }

      @Override public int compare(File a, File b) {
        return collator.compare(a.getName(), b.getName());
      }
    }

    Locale original = Locale.getDefault();
    try {
      tmp().createFile("a");
      tmp().createFile("b");
      tmp().createFile("你");
      tmp().createFile("好");

      Locale.setDefault(ENGLISH);
      verify(tmp().get(), SORT_BY_NAME,
          new NameCollator(Collator.getInstance(ENGLISH)));

      Locale.setDefault(SIMPLIFIED_CHINESE);
      verify(tmp().get(), SORT_BY_NAME,
          new NameCollator(Collator.getInstance(SIMPLIFIED_CHINESE)));

    } finally {
      Locale.setDefault(original);
    }
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
    try (Cursor cursor = queryChildren()) {

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

  private void verify(File dir, String order, Comparator<? super File> comparator)
      throws IOException {
    verify(dir, order, comparator, true, dir.listFiles());
  }

  private void verify(
      File dir,
      String order,
      Comparator<? super File> comparator,
      boolean showHidden,
      File... files) throws IOException {
    try (Cursor cursor = queryChildren(dir, showHidden, order)) {
      sort(files, comparator);
      verify(cursor, files);
    }
  }

  private void verify(File dir) throws IOException {
    verify(dir, SORT_BY_NAME, NAME_COMPARATOR, true, dir.listFiles());
  }

  private static void verify(Cursor cursor, File[] files) throws IOException {
    List<String> expected = getIds(files);
    List<String> actual = getIds(cursor);
    assertEquals(expected, actual);
    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      File file = files[cursor.getPosition()];
      LocalFileStatus info = LocalFileStatus.read(file.getPath());
      assertEquals(FilesContract.getFileId(file), Files.id(cursor));
      assertEquals(info.name(), Files.name(cursor));
      assertEquals(info.modified(), Files.modified(cursor));
      assertEquals(info.size(), Files.length(cursor));
      assertEquals(info.isDirectory(), Files.isDirectory(cursor));
      assertEquals(info.isReadable(), Files.isReadable(cursor));
      assertEquals(info.isWritable(), Files.isWritable(cursor));

      if (info.isRegularFile()) {
        assertEquals(TYPE_REGULAR_FILE, Files.type(cursor));
      } else if (info.isSymbolicLink()) {
        assertEquals(TYPE_SYMLINK, Files.type(cursor));
      } else if (info.isDirectory()) {
        assertEquals(TYPE_DIRECTORY, Files.type(cursor));
      } else {
        assertEquals(TYPE_UNKNOWN, Files.type(cursor));
      }
    }
  }

  private static List<String> getIds(Cursor cursor) {
    List<String> names = newArrayListWithCapacity(cursor.getCount());
    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      names.add(Files.id(cursor));
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
    Uri uri = getFilesUri(context, FilesContract.getFileId(dir), showHidden);
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
    Uri uri = getSelectionUri(context, ids);
    return resolver.query(uri, null, null, null, null);
  }
}
