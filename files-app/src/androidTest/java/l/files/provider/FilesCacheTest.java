package l.files.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;

import l.files.common.testing.FileBaseTest;
import l.files.fs.Path;
import l.files.fs.WatchEvent;
import l.files.fs.WatchService;
import l.files.fs.local.LocalPath;
import l.files.fs.local.LocalWatchService;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.timeout;
import static l.files.fs.WatchEvent.Kind.MODIFY;
import static l.files.provider.FilesContract.Files;
import static l.files.provider.FilesContract.Files.COLUMNS;
import static l.files.provider.FilesContract.Files.SORT_BY_NAME;
import static l.files.provider.FilesContract.getFileUri;
import static l.files.provider.FilesContract.getFilesUri;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class FilesCacheTest extends FileBaseTest {

  private ContentResolver resolver;
  private LocalWatchService service;
  private FilesCache cache;

  @Override protected void setUp() throws Exception {
    super.setUp();
    resolver = mock(ContentResolver.class);
    service = LocalWatchService.create();
    cache = new FilesCache(getContext(), service, resolver);
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    service.close();
  }

  public void testDoesNotCacheIgnoredLocations() throws Exception {
    File ignoredDir = tmp().createDir("ignored");
    Path ignoredPath = LocalPath.of(ignoredDir);
    WatchService service = mock(LocalWatchService.class);
    given(service.isWatchable(not(eq(ignoredPath)))).willReturn(true);
    given(service.isWatchable(eq(ignoredPath))).willReturn(false);

    FilesCache cache = new FilesCache(getContext(), service, resolver);
    Uri uri = getFilesUri(getContext(), ignoredDir, true);
    assertNull(cache.getFromCache(ignoredPath));
    // noinspection UnusedDeclaration
    try (Cursor cursor = cache.get(uri, null, null, null)) {
      assertNull(cache.getFromCache(ignoredPath));
    }
  }

  public void testCacheIsLiveWhenInUse() throws Exception {
    tmp().createFile("a");
    Path path = LocalPath.of(tmp().get());
    Uri uri = getFilesUri(getContext(), tmp().get(), true);
    assertNull(cache.getFromCache(path));

    // Holding a reference to cursor, this should kept the cache alive
    // noinspection UnusedDeclaration
    try (Cursor cursor = cache.get(uri, null, null, null)) {
      assertNotNull(cache.getFromCache(path));
      byte[] data = new byte[256];
      while (true) {
        sleep(50);
        try {
          data = new byte[data.length * 2];
        } catch (OutOfMemoryError e) {
          assertNotNull(cache.getFromCache(path));
          break;
        }
      }
    }
  }

  public void testCacheIsClearedWhenNotUsed() throws Exception {
    tmp().createFile("a");
    LocalPath path = LocalPath.of(tmp().get());
    Uri uri = getFilesUri(getContext(), tmp().get(), true);
    assertNull(cache.getFromCache(path));

    // No reference is hold onto the cursor, this will cause the cache to clear
    cache.get(uri, null, null, null).close();
    assertNotNull(cache.getFromCache(path));
    byte[] data = new byte[256];
    while (true) {
      sleep(50);
      try {
        data = new byte[data.length * 2];
      } catch (OutOfMemoryError e) {
        assertNull(cache.getFromCache(path));
        break;
      }
    }
  }

  public void testUpdatesCacheOnFileAdded() throws Exception {
    final File a = tmp().createFile("a");
    final Uri uri = getFilesUri(getContext(), tmp().get(), true);
    try (Cursor cursor = cache.get(uri, COLUMNS, null, null)) {
      assertTrue(cursor.moveToFirst());
      assertEquals(a.getName(), Files.name(cursor));

      final File b = tmp().createFile("b");
      timeout(1, SECONDS, new Runnable() {
        @Override public void run() {
          try (Cursor cursor = cache.get(uri, COLUMNS, SORT_BY_NAME, null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(a.getName(), Files.name(cursor));
            assertTrue(cursor.moveToNext());
            assertEquals(b.getName(), Files.name(cursor));
            verify(resolver).notifyChange(buildRootNotificationUri(), null);
          }
        }
      });
    }
  }

  public void testUpdatesCacheOnFileRemoved() throws Exception {
    final File a = tmp().createFile("a");
    final File b = tmp().createFile("b");
    final Uri uri = getFilesUri(getContext(), tmp().get(), true);
    try (Cursor cursor = cache.get(uri, COLUMNS, SORT_BY_NAME, null)) {
      assertTrue(cursor.moveToFirst());
      assertEquals(a.getName(), Files.name(cursor));
      assertTrue(cursor.moveToNext());
      assertEquals(b.getName(), Files.name(cursor));

      assertTrue(b.delete());
      timeout(1, SECONDS, new Runnable() {
        @Override public void run() {
          try (Cursor cursor = cache.get(uri, COLUMNS, SORT_BY_NAME, null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(a.getName(), Files.name(cursor));
            assertEquals(1, cursor.getCount());
            verify(resolver).notifyChange(buildRootNotificationUri(), null);
          }
        }
      });
    }
  }

  public void testUpdatesCacheOnFileChanged() throws Exception {
    final File file = tmp().createFile("test");
    final Uri uri = getFilesUri(getContext(), tmp().get(), true);
    try (Cursor cursor = cache.get(uri, COLUMNS, null, null)) {
      assertTrue(cursor.moveToFirst());
      assertEquals(file.length(), Files.length(cursor));

      write("hello world", file, UTF_8);
      timeout(1, SECONDS, new Runnable() {
        @Override public void run() {
          try (Cursor cursor = cache.get(uri, COLUMNS, null, null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(file.length(), Files.length(cursor));
            verify(resolver).notifyChange(buildRootNotificationUri(), null);
          }
        }
      });
    }
  }

  public void testDoesNotNotifyIfFileIsNotChanged() throws Exception {
    File file = tmp().createFile("test");
    Uri uri = getFilesUri(getContext(), tmp().get(), true);
    try (Cursor cursor = cache.get(uri, COLUMNS, null, null)) {
      assertTrue(cursor.moveToFirst());
      assertEquals(file.length(), Files.length(cursor));

      cache.onEvent(WatchEvent.create(MODIFY, LocalPath.of(file)));
      sleep(1000);
      verifyZeroInteractions(resolver);
    }
  }

  private Uri buildRootNotificationUri() {
    return getFileUri(getContext(), tmp().get());
  }
}
