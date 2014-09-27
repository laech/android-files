package l.files.provider.bookmarks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.util.Collections;

import l.files.common.testing.FileBaseTest;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static java.util.Arrays.sort;
import static l.files.provider.FilesContract.Files;
import static l.files.provider.FilesContract.Files.SORT_BY_NAME;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.bookmarks.BookmarksContract.getBookmarkUri;
import static l.files.provider.bookmarks.BookmarksContract.getBookmarksUri;
import static org.apache.commons.io.comparator.NameFileComparator.NAME_COMPARATOR;

public final class BookmarksProviderTest extends FileBaseTest {

  @Override protected void setUp() throws Exception {
    super.setUp();
    assertTrue(getDefaultSharedPreferences(getContext())
        .edit()
        .putStringSet(Bookmarks.KEY, Collections.<String>emptySet())
        .commit());
  }

  public void testQueryBookmarkWithIdReturnsEmptyCursorIfNotBookmarked() {
    Uri uri = getBookmarkUri(getContext(), getFileId(tmp().get()));
    try (Cursor cursor = resolver().query(uri, null, null, null, null)) {
      assertEquals(0, cursor.getCount());
    }
  }

  public void testQueryBookmarkWithId() {
    File dir = tmp().createDir("a");
    insertBookmarks(dir);
    Uri uri = getBookmarkUri(getContext(), getFileId(dir));
    try (Cursor cursor = resolver().query(uri, null, null, null, null)) {
      assertTrue(cursor.moveToFirst());
      assertEquals(dir.getName(), Files.name(cursor));
      assertEquals(1, cursor.getCount());
    }
  }

  public void testBookmark() {
    File[] files = {
        tmp().createDir("a"),
        tmp().createDir("b")
    };

    insertBookmarks(files);
    assertBookmarks(files);

    deleteBookmarks(files);
    assertBookmarks();
  }

  private void deleteBookmarks(File... files) {
    for (File file : files) {
      Uri uri = getBookmarkUri(getContext(), getFileId(file));
      resolver().delete(uri, null, null);
    }
  }

  private void insertBookmarks(File... files) {
    for (File file : files) {
      Uri uri = getBookmarkUri(getContext(), getFileId(file));
      resolver().insert(uri, null);
    }
  }

  private void assertBookmarks(File... files) {
    File[] expected = files.clone();
    sort(expected, NAME_COMPARATOR);

    Uri uri = getBookmarksUri(getContext());
    try (Cursor cursor = resolver().query(uri, null, null, null, SORT_BY_NAME)) {

      assertEquals(files.length, cursor.getCount());
      for (int i = 0; i < files.length; i++) {
        assertTrue(cursor.moveToPosition(i));
        assertEquals(files[i].getName(), Files.name(cursor));
      }
      assertFalse(cursor.moveToNext());

    }
  }

  private ContentResolver resolver() {
    return getContext().getContentResolver();
  }
}
