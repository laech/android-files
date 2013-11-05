package l.files.provider;

import android.database.AbstractCursor;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static l.files.provider.FilesContract.FileInfo.COLUMN_LAST_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.COLUMN_MEDIA_TYPE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_NAME;
import static l.files.provider.FilesContract.FileInfo.COLUMN_READABLE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_SIZE;
import static l.files.provider.FilesContract.FileInfo.COLUMN_URI;
import static l.files.provider.FilesContract.FileInfo.COLUMN_WRITABLE;
import static l.files.provider.FilesContract.FileInfo.MEDIA_TYPE_DIR;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class FileCursor extends AbstractCursor {

  private final File[] files;
  private final String[] columns;
  private final Map<File, Info> infos;

  FileCursor(File[] files, String[] columns) {
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
    if (COLUMN_URI.equals(col)) return info.uri();
    if (COLUMN_NAME.equals(col)) return info.name();
    if (COLUMN_MEDIA_TYPE.equals(col)) return info.mime();
    throw new IllegalArgumentException();
  }

  @Override public int getInt(int column) {
    Info info = getCurrentFileInfo();
    String col = columns[column];
    if (COLUMN_READABLE.equals(col)) return info.readable();
    if (COLUMN_WRITABLE.equals(col)) return info.writable();
    throw new IllegalArgumentException();
  }

  @Override public long getLong(int column) {
    Info info = getCurrentFileInfo();
    String col = columns[column];
    if (COLUMN_SIZE.equals(col)) return info.length();
    if (COLUMN_LAST_MODIFIED.equals(col)) return info.modified();
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

  private static final class Info {
    private String uri;
    private String name;
    private String mediaType;
    private int canRead = -1;
    private int canWrite = -1;
    private long length = -1;
    private long lastModified = 1;

    private final File file;

    Info(File file) {
      this.file = file;
    }

    String uri() {
      if (uri == null) uri = "file:" + file.getAbsolutePath();
      return uri;
    }

    String name() {
      if (name == null) name = file.getName();
      return name;
    }

    String mime() {
      if (mediaType == null) {
        mediaType = file.isDirectory()
            ? MEDIA_TYPE_DIR
            : MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(getExtension(name));
      }
      return mediaType;
    }

    long length() {
      if (length < 0) length = file.length();
      return length;
    }

    long modified() {
      if (lastModified < 0) lastModified = file.lastModified();
      return lastModified;
    }

    int readable() {
      if (canRead < 0) canRead = file.canRead() ? 1 : 0;
      return canRead;
    }

    int writable() {
      if (canWrite < 0) canWrite = file.canWrite() ? 1 : 0;
      return canWrite;
    }
  }
}
