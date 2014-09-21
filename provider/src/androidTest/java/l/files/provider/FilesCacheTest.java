package l.files.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;

import l.files.common.testing.FileBaseTest;
import l.files.io.file.Path;
import l.files.io.file.WatchEvent;
import l.files.io.file.WatchService;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static com.google.common.truth.Truth.ASSERT;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.common.testing.Tests.timeout;
import static l.files.io.file.WatchEvent.Kind.MODIFY;
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
  private WatchService service;
  private FilesCache cache;

  @Override protected void setUp() throws Exception {
    super.setUp();
    resolver = mock(ContentResolver.class);
    service = WatchService.create();
    cache = new FilesCache(getContext(), service, resolver);
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    service.close();
  }

  public void testDoesNotCacheIgnoredLocations() throws Exception {
    File ignoredDir = tmp().createDir("ignored");
    Path ignoredPath = Path.from(ignoredDir);
    WatchService service = mock(WatchService.class);
    given(service.isWatchable(not(eq(ignoredPath)))).willReturn(true);
    given(service.isWatchable(eq(ignoredPath))).willReturn(false);

    FilesCache cache = new FilesCache(getContext(), service, resolver);
    Uri uri = getFilesUri(getContext(), ignoredDir, true);
    ASSERT.that(cache.getFromCache(ignoredPath)).isNull();
    // noinspection UnusedDeclaration
    try (Cursor cursor = cache.get(uri, null, null, null)) {
      ASSERT.that(cache.getFromCache(ignoredPath)).isNull();
    }
  }

  public void testCacheIsLiveWhenInUse() throws Exception {
    tmp().createFile("a");
    Path path = Path.from(tmp().get());
    Uri uri = getFilesUri(getContext(), tmp().get(), true);
    ASSERT.that(cache.getFromCache(path)).isNull();

    // Holding a reference to cursor, this should kept the cache alive
    // noinspection UnusedDeclaration
    try (Cursor cursor = cache.get(uri, null, null, null)) {
      ASSERT.that(cache.getFromCache(path)).isNotNull();
      byte[] data = new byte[256];
      while (true) {
        sleep(50);
        try {
          data = new byte[data.length * 2];
        } catch (OutOfMemoryError e) {
          ASSERT.that(cache.getFromCache(path)).isNotNull();
          break;
        }
      }
    }
  }

  public void testCacheIsClearedWhenNotUsed() throws Exception {
    tmp().createFile("a");
    Path path = Path.from(tmp().get());
    Uri uri = getFilesUri(getContext(), tmp().get(), true);
    ASSERT.that(cache.getFromCache(path)).isNull();

    // No reference is hold onto the cursor, this will cause the cache to clear
    cache.get(uri, null, null, null).close();
    ASSERT.that(cache.getFromCache(path)).isNotNull();
    byte[] data = new byte[256];
    while (true) {
      sleep(50);
      try {
        data = new byte[data.length * 2];
      } catch (OutOfMemoryError e) {
        ASSERT.that(cache.getFromCache(path)).isNull();
        break;
      }
    }
  }

  public void testUpdatesCacheOnFileAdded() throws Exception {
    final File a = tmp().createFile("a");
    final Uri uri = getFilesUri(getContext(), tmp().get(), true);
    try (Cursor cursor = cache.get(uri, COLUMNS, null, null)) {
      ASSERT.that(cursor.moveToFirst()).isTrue();
      ASSERT.that(Files.name(cursor)).is(a.getName());

      final File b = tmp().createFile("b");
      timeout(1, SECONDS, new Runnable() {
        @Override public void run() {
          try (Cursor cursor = cache.get(uri, COLUMNS, SORT_BY_NAME, null)) {
            ASSERT.that(cursor.moveToFirst()).isTrue();
            ASSERT.that(Files.name(cursor)).is(a.getName());
            ASSERT.that(cursor.moveToNext()).isTrue();
            ASSERT.that(Files.name(cursor)).is(b.getName());
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
      ASSERT.that(cursor.moveToFirst()).isTrue();
      ASSERT.that(Files.name(cursor)).is(a.getName());
      ASSERT.that(cursor.moveToNext()).isTrue();
      ASSERT.that(Files.name(cursor)).is(b.getName());

      ASSERT.that(b.delete()).isTrue();
      timeout(1, SECONDS, new Runnable() {
        @Override public void run() {
          try (Cursor cursor = cache.get(uri, COLUMNS, SORT_BY_NAME, null)) {
            ASSERT.that(cursor.moveToFirst()).isTrue();
            ASSERT.that(Files.name(cursor)).is(a.getName());
            ASSERT.that(cursor.getCount()).is(1);
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
      ASSERT.that(cursor.moveToFirst()).isTrue();
      ASSERT.that(Files.length(cursor)).is(file.length());

      write("hello world", file, UTF_8);
      timeout(1, SECONDS, new Runnable() {
        @Override public void run() {
          try (Cursor cursor = cache.get(uri, COLUMNS, null, null)) {
            ASSERT.that(cursor.moveToFirst()).isTrue();
            ASSERT.that(Files.length(cursor)).is(file.length());
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
      ASSERT.that(cursor.moveToFirst()).isTrue();
      ASSERT.that(Files.length(cursor)).is(file.length());

      cache.onEvent(WatchEvent.create(MODIFY, Path.from(file)));
      sleep(1000);
      verifyZeroInteractions(resolver);
    }
  }

  private Uri buildRootNotificationUri() {
    return getFileUri(getContext(), tmp().get());
  }
}
