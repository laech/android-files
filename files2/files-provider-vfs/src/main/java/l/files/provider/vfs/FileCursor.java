package l.files.provider.vfs;

import android.database.AbstractCursor;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static l.files.provider.FilesContract.FileInfo.COLUMN_FILE_ID;
import static l.files.provider.FilesContract.FileInfo.COLUMN_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.COLUMN_MEDIA_TYPE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.FileInfo.COLUMN_READABLE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_SIZE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_WRITABLE;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class FileCursor extends AbstractCursor {

  private final File[] files;
  private final String[] columns;
  private final Map<File, Info> infos;

  public FileCursor(File[] files, String[] columns) {
    this.files = files;
    this.columns = columns;
    this.infos = new HashMap<>();
  }

  private Info getCurrentFileInfo() {
    checkPosition();
    File file = files[getPosition()];
    Info info = infos.get(file);
    if (info == null) {
      info = new Info(file);
      infos.put(file, info);
    }
    return info;
  }

  @Override public int getCount() {
    return files.length;
  }

  @Override public String[] getColumnNames() {
    return columns;
  }

  @Override public String getString(int column) {
    switch (columns[column]) {
      case COLUMN_FILE_ID:
        return getCurrentFileInfo().path;
      case COLUMN_MEDIA_TYPE:
        return getCurrentFileInfo().mediaType;
      case COLUMN_NAME:
        return getCurrentFileInfo().name;
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override public int getInt(int column) {
    switch (columns[column]) {
      case COLUMN_READABLE:
        return getCurrentFileInfo().canRead ? 1 : 0;
      case COLUMN_WRITABLE:
        return getCurrentFileInfo().canWrite ? 1 : 0;
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override public long getLong(int column) {
    switch (columns[column]) {
      case COLUMN_LAST_MODIFIED:
        return getCurrentFileInfo().lastModified;
      case COLUMN_SIZE:
        return getCurrentFileInfo().length;
      default:
        throw new IllegalArgumentException();
    }
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

  @Override public void close() {
    super.close();
  }

  private static final class Info {
    final String path;
    final String name;
    final String mediaType;
    final long length;
    final long lastModified;
    final boolean canRead;
    final boolean canWrite;

    Info(File file) {
      path = file.getPath();
      name = file.getName();
      length = file.length();
      lastModified = file.lastModified();
      canRead = file.canRead();
      canWrite = file.canWrite();
      mediaType = file.isDirectory()
          ? MEDIA_TYPE_DIR
          : MimeTypeMap.getSingleton()
          .getMimeTypeFromExtension(getExtension(name));
    }
  }
}
