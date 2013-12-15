package l.files.app;

import android.database.Cursor;

import l.files.common.base.ValueObject;
import l.files.provider.FileCursors;

import static com.google.common.base.Preconditions.checkNotNull;

final class OpenFileRequest extends ValueObject {

  private final String fileId;
  private final String filename;
  private final boolean canRead;
  private final boolean directory;

  public OpenFileRequest(
      String fileId, String filename, boolean canRead, boolean directory) {
    this.filename = checkNotNull(filename, "filename");
    this.fileId = checkNotNull(fileId, "fileId");
    this.canRead = canRead;
    this.directory = directory;
  }

  public static OpenFileRequest from(Cursor cursor) {
    return new OpenFileRequest(
        FileCursors.getFileId(cursor),
        FileCursors.getFileName(cursor),
        FileCursors.isReadable(cursor),
        FileCursors.isDirectory(cursor));
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
