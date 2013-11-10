package l.files.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import org.apache.tika.Tika;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;
import static l.files.provider.FilesContract.MATCH_FILES_CHILDREN;
import static l.files.provider.FilesContract.MATCH_FILES_ID;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.FilesContract.newMatcher;
import static l.files.provider.FilesContract.toURI;

public final class FilesProvider extends ContentProvider {

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

  @Override public Cursor query(
      Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {

    if (projection == null) {
      projection = DEFAULT_COLUMNS;
    }

    switch (matcher.match(uri)) {
      case MATCH_FILES_CHILDREN:
        File[] children = new File(toURI(getFileId(uri))).listFiles();
        if (children == null) children = new File[0];
        return new FileCursor(children, projection);
      default:
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
    }
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

  private static class TikaHolder {
    static final Tika TIKA = new Tika();
  }
}
