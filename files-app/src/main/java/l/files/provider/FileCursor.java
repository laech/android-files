package l.files.provider;

import java.io.File;

import l.files.common.database.BaseCursor;
import l.files.fs.FileStatus;

import static l.files.common.database.DataTypes.booleanToInt;
import static l.files.provider.FilesContract.Files.ID;
import static l.files.provider.FilesContract.Files.LENGTH;
import static l.files.provider.FilesContract.Files.MIME;
import static l.files.provider.FilesContract.Files.MODIFIED;
import static l.files.provider.FilesContract.Files.NAME;
import static l.files.provider.FilesContract.Files.READABLE;
import static l.files.provider.FilesContract.Files.TYPE;
import static l.files.provider.FilesContract.Files.TYPE_DIRECTORY;
import static l.files.provider.FilesContract.Files.TYPE_REGULAR_FILE;
import static l.files.provider.FilesContract.Files.TYPE_SYMLINK;
import static l.files.provider.FilesContract.Files.TYPE_UNKNOWN;
import static l.files.provider.FilesContract.Files.WRITABLE;
import static l.files.provider.FilesContract.getFileId;

final class FileCursor extends BaseCursor {

  private final FileStatus[] files;
  private final String[] columns;

  public FileCursor(FileStatus[] files, String[] columns) {
    this.files = files;
    this.columns = columns;
  }

  private FileStatus getCurrentFile() {
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
    FileStatus file = getCurrentFile();
    String col = columns[column];
    if (ID.equals(col)) return getFileId(new File(file.path().uri())); // TODO
    if (NAME.equals(col)) return file.name();
    if (MIME.equals(col)) return file.basicMediaType().toString();
    if (TYPE.equals(col)) return getType(file);
    throw new IllegalArgumentException();
  }

  private String getType(FileStatus file) {
    if (file.isDirectory()) return TYPE_DIRECTORY;
    if (file.isSymbolicLink()) return TYPE_SYMLINK;
    if (file.isRegularFile()) return TYPE_REGULAR_FILE;
    return TYPE_UNKNOWN;
  }

  @Override public int getInt(int column) {
    FileStatus file = getCurrentFile();
    String col = columns[column];
    if (READABLE.equals(col)) return booleanToInt(file.isReadable());
    if (WRITABLE.equals(col)) return booleanToInt(file.isWritable());
    throw new IllegalArgumentException();
  }

  @Override public long getLong(int column) {
    FileStatus file = getCurrentFile();
    String col = columns[column];
    if (LENGTH.equals(col)) return file.size();
    if (MODIFIED.equals(col)) return file.lastModifiedTime().getMillis();
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
