package l.files.provider;

import android.database.AbstractCursor;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static l.files.provider.FilesContract.FileInfo.*;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class FileCursor extends AbstractCursor {

  private final File[] files;
  private final String[] columns;
  private final Map<File, Info> infos;

  public FileCursor(File[] files, String[] columns) {
    this.files = files;
    this.columns = columns;
    this.infos = newHashMap();
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
    Info info = getCurrentFileInfo();
    String col = columns[column];
    if (COLUMN_NAME.equals(col)) return info.name;
    if (COLUMN_FILE_ID.equals(col)) return info.path;
    if (COLUMN_MEDIA_TYPE.equals(col)) return info.mediaType;
    throw new IllegalArgumentException();
  }

  @Override public int getInt(int column) {
    Info info = getCurrentFileInfo();
    String col = columns[column];
    if (COLUMN_READABLE.equals(col)) return info.canRead ? 1 : 0;
    if (COLUMN_WRITABLE.equals(col)) return info.canWrite ? 1 : 0;
    throw new IllegalArgumentException();
  }

  @Override public long getLong(int column) {
    Info info = getCurrentFileInfo();
    String col = columns[column];
    if (COLUMN_SIZE.equals(col)) return info.length;
    if (COLUMN_LAST_MODIFIED.equals(col)) return info.lastModified;
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
