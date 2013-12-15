package l.files.provider;

import android.database.Cursor;

import static l.files.common.database.Cursors.getInt;
import static l.files.common.database.Cursors.getLong;
import static l.files.common.database.Cursors.getString;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.FileInfo.COLUMN_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.COLUMN_MEDIA_TYPE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.FileInfo.COLUMN_READABLE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_SIZE;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;

public final class FileCursors {
  private FileCursors() {}

  public static String getFileId(Cursor cursor) {
    return getString(cursor, COLUMN_ID);
  }

  public static String getMediaType(Cursor cursor) {
    return getString(cursor, COLUMN_MEDIA_TYPE);
  }

  public static String getFileName(Cursor cursor) {
    return getString(cursor, COLUMN_NAME);
  }

  public static long getLastModified(Cursor cursor) {
    return getLong(cursor, COLUMN_LAST_MODIFIED);
  }

  public static long getSize(Cursor cursor) {
    return getLong(cursor, COLUMN_SIZE);
  }

  public static boolean isReadable(Cursor cursor) {
    return getInt(cursor, COLUMN_READABLE) == 1;
  }

  public static boolean isDirectory(Cursor cursor) {
    return MEDIA_TYPE_DIR.equals(getMediaType(cursor));
  }
}
