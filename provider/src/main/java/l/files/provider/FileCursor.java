package l.files.provider;

import l.files.common.database.BaseCursor;
import l.files.io.file.FileInfo;

import static l.files.common.database.DataTypes.booleanToInt;
import static l.files.provider.FilesContract.FileInfo.LOCATION;
import static l.files.provider.FilesContract.FileInfo.MIME;
import static l.files.provider.FilesContract.FileInfo.MODIFIED;
import static l.files.provider.FilesContract.FileInfo.NAME;
import static l.files.provider.FilesContract.FileInfo.READABLE;
import static l.files.provider.FilesContract.FileInfo.SIZE;
import static l.files.provider.FilesContract.FileInfo.WRITABLE;

final class FileCursor extends BaseCursor {

  private final FileInfo[] files;
  private final String[] columns;

  public FileCursor(FileInfo[] files, String[] columns) {
    this.files = files;
    this.columns = columns;
  }

  private FileInfo getCurrentFile() {
    checkPosition();
    return files[getPosition()];
  }

  @Override public int getCount() {
    return files.length;
  }

  @Override public String[] getColumnNames() {
    return columns;
  }

  @Override public String getString(int column) {
    FileInfo file = getCurrentFile();
    String col = columns[column];
    if (LOCATION.equals(col)) return file.getUri();
    if (NAME.equals(col)) return file.getName();
    if (MIME.equals(col)) return file.getMediaType();
    throw new IllegalArgumentException();
  }

  @Override public int getInt(int column) {
    FileInfo file = getCurrentFile();
    String col = columns[column];
    if (READABLE.equals(col)) return booleanToInt(file.isReadable());
    if (WRITABLE.equals(col)) return booleanToInt(file.isWritable());
    throw new IllegalArgumentException();
  }

  @Override public long getLong(int column) {
    FileInfo file = getCurrentFile();
    String col = columns[column];
    if (SIZE.equals(col)) return file.getSize();
    if (MODIFIED.equals(col)) return file.getLastModified();
    throw new IllegalArgumentException();
  }

  @Override public short getShort(int column) {
    throw new IllegalArgumentException();
  }

  @Override public float getFloat(int column) {
    throw new IllegalArgumentException();
  }

  @Override public double getDouble(int column) {
    throw new IllegalArgumentException();
  }

  @Override public boolean isNull(int column) {
    return false;
  }
}
