package l.files.app;

import android.database.Cursor;

import l.files.common.base.ValueObject;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.provider.FilesContract.FileInfo.COLUMN_ID;
import static l.files.provider.FilesContract.FileInfo.COLUMN_MEDIA_TYPE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.FileInfo.COLUMN_READABLE;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;

final class OpenFileRequest extends ValueObject {

  private final String fileId;
  private final String filename;
  private final boolean canRead;
  private final boolean directory;

  public OpenFileRequest(String fileId, String filename, boolean canRead, boolean directory) {
    this.filename = checkNotNull(filename, "filename");
    this.fileId = checkNotNull(fileId, "fileId");
    this.canRead = canRead;
    this.directory = directory;
  }

  public static OpenFileRequest from(Cursor cursor) {
    return new OpenFileRequest(
        getFileId(cursor),
        getFilename(cursor),
        canRead(cursor),
        isDirectory(cursor));
  }

  private static String getFileId(Cursor cursor) {
    int idColumn = cursor.getColumnIndex(COLUMN_ID);
    return cursor.getString(idColumn);
  }

  private static String getFilename(Cursor cursor) {
    int nameColumn = cursor.getColumnIndex(COLUMN_NAME);
    return cursor.getString(nameColumn);
  }

  private static boolean canRead(Cursor cursor) {
    int readableColumn = cursor.getColumnIndex(COLUMN_READABLE);
    return cursor.getInt(readableColumn) == 1;
  }

  private static boolean isDirectory(Cursor cursor) {
    int nameColumn = cursor.getColumnIndex(COLUMN_MEDIA_TYPE);
    return cursor.getString(nameColumn).equals(MEDIA_TYPE_DIR);
  }

  public String fileId() {
    return fileId;
  }

  public String filename() {
    return filename;
  }

  public boolean isDirectory() {
    return directory;
  }

  public boolean canRead() {
    return canRead;
  }
}
