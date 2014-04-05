package l.files.provider;

import l.files.common.database.BaseCursor;

import static l.files.provider.FilesContract.FileInfo.SIZE;
import static l.files.provider.FilesContract.FileInfo.LOCATION;
import static l.files.provider.FilesContract.FileInfo.MIME;
import static l.files.provider.FilesContract.FileInfo.MODIFIED;
import static l.files.provider.FilesContract.FileInfo.NAME;
import static l.files.provider.FilesContract.FileInfo.READABLE;
import static l.files.provider.FilesContract.FileInfo.WRITABLE;

final class FileCursor extends BaseCursor {

  private final FileData[] files;
  private final String[] columns;
  private final Object tag; // TODO

  FileCursor(FileData[] files, String[] columns) {
    this(files, columns, null);
  }

  FileCursor(FileData[] files, String[] columns, Object tag) {
    this.files = files;
    this.columns = columns;
    this.tag = tag;
  }

  private FileData getCurrentFile() {
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
    FileData file = getCurrentFile();
    String col = columns[column];
    if (LOCATION.equals(col)) return file.location;
    if (NAME.equals(col)) return file.name;
    if (MIME.equals(col)) return file.mime;
    throw new IllegalArgumentException();
  }

  @Override public int getInt(int column) {
    FileData file = getCurrentFile();
    String col = columns[column];
    if (READABLE.equals(col)) return file.canRead;
    if (WRITABLE.equals(col)) return file.canWrite;
    throw new IllegalArgumentException();
  }

  @Override public long getLong(int column) {
    FileData file = getCurrentFile();
    String col = columns[column];
    if (SIZE.equals(col)) return file.length;
    if (MODIFIED.equals(col)) return file.lastModified;
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
