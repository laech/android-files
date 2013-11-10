package l.files.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import org.apache.tika.Tika;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.provider.Bookmarks.getBookmark;
import static l.files.provider.Bookmarks.getBookmarks;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;
import static l.files.provider.FilesContract.MATCH_BOOKMARKS;
import static l.files.provider.FilesContract.MATCH_BOOKMARKS_ID;
import static l.files.provider.FilesContract.MATCH_FILES_CHILDREN;
import static l.files.provider.FilesContract.MATCH_FILES_ID;
import static l.files.provider.FilesContract.buildBookmarksUri;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.FilesContract.newMatcher;
import static l.files.provider.FilesContract.toURI;

public final class FilesProvider extends ContentProvider
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String PREF_BOOKMARKS = "bookmarks";

  private static final String[] DEFAULT_COLUMNS = {
      FileInfo.COLUMN_ID,
      FileInfo.COLUMN_NAME,
      FileInfo.COLUMN_SIZE,
      FileInfo.COLUMN_READABLE,
      FileInfo.COLUMN_WRITABLE,
      FileInfo.COLUMN_MEDIA_TYPE,
      FileInfo.COLUMN_LAST_MODIFIED,
  };

  private static final UriMatcher matcher = newMatcher();

  @Override public boolean onCreate() {
    getPreference().registerOnSharedPreferenceChangeListener(this);
    return true;
  }

  @Override public ParcelFileDescriptor openFile(Uri uri, String mode)
      throws FileNotFoundException {
    switch (matcher.match(uri)) {
      case MATCH_FILES_ID:
        File file = new File(toURI(getFileId(uri)));
        return ParcelFileDescriptor.open(file, MODE_READ_ONLY);
    }
    return super.openFile(uri, mode);
  }

  @Override public Cursor query(Uri uri, String[] projection, String selection,
                                String[] selectionArgs, String sortOrder) {

    if (projection == null) projection = DEFAULT_COLUMNS;

    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS:
        return queryBookmarks(uri, projection);
      case MATCH_BOOKMARKS_ID:
        return queryBookmark(uri, projection);
      case MATCH_FILES_CHILDREN:
        return queryFiles(uri, projection);
      default:
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
    }
  }

  private Cursor queryBookmark(Uri uri, String[] projection) {
    File[] bookmark = getBookmark(getPreference(), getFileId(uri));
    return newCursor(uri, projection, bookmark);
  }

  private Cursor queryBookmarks(Uri uri, String[] projection) {
    File[] bookmarks = getBookmarks(getPreference());
    return newCursor(uri, projection, bookmarks);
  }

  private Cursor queryFiles(Uri uri, String[] projection) {
    File[] children = new File(toURI(getFileId(uri))).listFiles();
    if (children == null) children = new File[0];
    return newCursor(uri, projection, children);
  }

  private Cursor newCursor(Uri uri, String[] projection, File[] files) {
    Cursor c = new FileCursor(files, projection);
    c.setNotificationUri(getContentResolver(), uri);
    return c;
  }

  private ContentResolver getContentResolver() {
    return getContext().getContentResolver();
  }

  @Override public String getType(Uri uri) {
    switch (matcher.match(uri)) {
      case MATCH_FILES_ID:
        String fileId = getFileId(uri);
        File file = new File(toURI(fileId));
        if (file.isDirectory()) {
          return MEDIA_TYPE_DIR;
        }
        try {
          return TikaHolder.TIKA.detect(file);
        } catch (IOException e) {
          return null;
        }
    }
    throw new UnsupportedOperationException("Unsupported Uri: " + uri);
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS_ID:
        Bookmarks.add(getPreference(), getFileId(uri));
        return uri;
    }
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    switch (matcher.match(uri)) {
      case MATCH_BOOKMARKS_ID:
        Bookmarks.remove(getPreference(), getFileId(uri));
        return 1;
    }
    return 0;
  }

  @Override public int update(
      Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    if (PREF_BOOKMARKS.equals(key))
      getContentResolver().notifyChange(buildBookmarksUri(), null);
  }

  private SharedPreferences getPreference() {
    return getDefaultSharedPreferences(getContext());
  }

  private static class TikaHolder {
    static final Tika TIKA = new Tika();
  }
}
