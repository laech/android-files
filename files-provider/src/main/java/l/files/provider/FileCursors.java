package l.files.provider;

import android.database.Cursor;

import static l.files.common.database.Cursors.getInt;
import static l.files.common.database.Cursors.getLong;
import static l.files.common.database.Cursors.getString;
import static l.files.provider.FilesContract.FileInfo.LOCATION;
import static l.files.provider.FilesContract.FileInfo.MODIFIED;
import static l.files.provider.FilesContract.FileInfo.MIME;
import static l.files.provider.FilesContract.FileInfo.NAME;
import static l.files.provider.FilesContract.FileInfo.READABLE;
import static l.files.provider.FilesContract.FileInfo.LENGTH;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;

public final class FileCursors {
  private FileCursors() {}

  public static String getLocation(Cursor cursor) {
    return getString(cursor, LOCATION);
  }

  public static String getMediaType(Cursor cursor) {
    return getString(cursor, MIME);
  }

  public static String getName(Cursor cursor) {
    return getString(cursor, NAME);
  }

  public static long getLastModified(Cursor cursor) {
    return getLong(cursor, MODIFIED);
  }

  public static long getSize(Cursor cursor) {
    return getLong(cursor, LENGTH);
  }

  public static boolean isReadable(Cursor cursor) {
    return getInt(cursor, READABLE) == 1;
  }

  public static boolean isDirectory(Cursor cursor) {
    return MIME_DIR.equals(getMediaType(cursor));
  }
}
