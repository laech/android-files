package l.files.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.net.URI;

import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.MATCH_PATHS_CHILDREN;
import static l.files.provider.FilesContract.getFileUri;
import static l.files.provider.FilesContract.newMatcher;

public final class FilesProvider extends ContentProvider {

  private static final String[] DEFAULT_COLUMNS = {
      FileInfo.COLUMN_URI,
      FileInfo.COLUMN_NAME,
      FileInfo.COLUMN_SIZE,
      FileInfo.COLUMN_READABLE,
      FileInfo.COLUMN_WRITABLE,
      FileInfo.COLUMN_MEDIA_TYPE,
      FileInfo.COLUMN_LAST_MODIFIED,
  };

  private static final UriMatcher matcher = newMatcher();

  @Override public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {
    if (projection == null) {
      projection = DEFAULT_COLUMNS;
    }
    switch (matcher.match(uri)) {
      case MATCH_PATHS_CHILDREN: {
        String fileUri = getFileUri(uri);
        File[] children = new File(URI.create(fileUri)).listFiles();
        if (children == null) {
          children = new File[0];
        }
        return new FileCursor(children, projection);
      }
      default:
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
    }
  }

  @Override public String getType(Uri uri) {
    return null;
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
    return 0;
  }
}
