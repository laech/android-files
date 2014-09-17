package l.files.provider;

import android.database.Cursor;

import static l.files.common.database.Cursors.getInt;
import static l.files.common.database.Cursors.getLong;
import static l.files.common.database.Cursors.getString;
import static l.files.common.database.DataTypes.intToBoolean;
import static l.files.provider.FilesContract.FileInfo.ID;
import static l.files.provider.FilesContract.FileInfo.MIME;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
import static l.files.provider.FilesContract.FileInfo.MODIFIED;
import static l.files.provider.FilesContract.FileInfo.NAME;
import static l.files.provider.FilesContract.FileInfo.READABLE;
import static l.files.provider.FilesContract.FileInfo.SIZE;
import static l.files.provider.FilesContract.FileInfo.WRITABLE;

public final class FileCursors {
  private FileCursors() {}

  /**
   * @deprecated use {@link #getId(Cursor)} instead
   */
  @Deprecated
  public static String getLocation(Cursor cursor) {
    return getId(cursor);
  }

  public static String getId(Cursor cursor) {
    return getString(cursor, ID);
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
    return getLong(cursor, SIZE);
  }

  public static boolean isReadable(Cursor cursor) {
    return intToBoolean(getInt(cursor, READABLE));
  }

  public static boolean isWritable(Cursor cursor) {
    return intToBoolean(getInt(cursor, WRITABLE));
  }

  public static boolean isDirectory(Cursor cursor) {
    return MIME_DIR.equals(getMediaType(cursor));
  }
}
