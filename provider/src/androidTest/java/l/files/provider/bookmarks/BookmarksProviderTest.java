package l.files.provider.bookmarks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.io.File;
import java.util.Collections;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static java.util.Arrays.sort;
import static l.files.provider.FilesContract.Files;
import static l.files.provider.FilesContract.Files.SORT_BY_NAME;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.bookmarks.BookmarksContract.buildBookmarkUri;
import static l.files.provider.bookmarks.BookmarksContract.buildBookmarksUri;
import static org.apache.commons.io.comparator.NameFileComparator.NAME_COMPARATOR;

public final class BookmarksProviderTest extends AndroidTestCase {

  @Override protected void setUp() throws Exception {
    super.setUp();
    assertTrue(getDefaultSharedPreferences(getContext())
        .edit()
        .putStringSet(Bookmarks.KEY, Collections.<String>emptySet())
        .commit());
  }

  public void testBookmark() {
    File[] files = {
        new File("/"),
        new File("/dev")
    };

    insertBookmarks(files);
    assertBookmarks(files);

    deleteBookmarks(files);
    assertBookmarks();
  }

  private void deleteBookmarks(File[] files) {
    for (File file : files) {
      Uri uri = buildBookmarkUri(getContext(), getFileId(file));
      resolver().delete(uri, null, null);
    }
  }

  private void insertBookmarks(File[] files) {
    for (File file : files) {
      Uri uri = buildBookmarkUri(getContext(), getFileId(file));
      resolver().insert(uri, null);
    }
  }

  private void assertBookmarks(File... files) {
    File[] expected = files.clone();
    sort(expected, NAME_COMPARATOR);

    Uri uri = buildBookmarksUri(getContext());
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
