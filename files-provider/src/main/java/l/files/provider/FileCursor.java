package l.files.provider;

import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Map;

import l.files.common.database.BaseCursor;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Locale.ENGLISH;
import static l.files.provider.FilesContract.FileInfo.LOCATION;
import static l.files.provider.FilesContract.FileInfo.MODIFIED;
import static l.files.provider.FilesContract.FileInfo.MIME;
import static l.files.provider.FilesContract.FileInfo.NAME;
import static l.files.provider.FilesContract.FileInfo.READABLE;
import static l.files.provider.FilesContract.FileInfo.LENGTH;
import static l.files.provider.FilesContract.FileInfo.WRITABLE;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
import static l.files.provider.FilesContract.getFileLocation;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class FileCursor extends BaseCursor {

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
    if (LOCATION.equals(col)) return info.id();
    if (NAME.equals(col)) return info.name();
    if (MIME.equals(col)) return info.mime();
    throw new IllegalArgumentException();
  }

  @Override public int getInt(int column) {
    Info info = getCurrentFileInfo();
    String col = columns[column];
    if (READABLE.equals(col)) return info.readable();
    if (WRITABLE.equals(col)) return info.writable();
    throw new IllegalArgumentException();
  }

  @Override public long getLong(int column) {
    Info info = getCurrentFileInfo();
    String col = columns[column];
    if (LENGTH.equals(col)) return info.length();
    if (MODIFIED.equals(col)) return info.modified();
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
    private static final int NONE = -1;
    private String id;
    private String name;
    private String mediaType;
    private int canRead = NONE;
    private int canWrite = NONE;
    private long length = NONE;
    private long lastModified = NONE;

    private final File file;

    Info(File file) {
      this.file = file;
    }

    String id() {
      if (id == null) id = getFileLocation(file);
      return id;
    }

    String name() {
      if (name == null) name = file.getName();
      if (isNullOrEmpty(name)) name = file.getPath();
      return name;
    }

    String mime() {
      if (mediaType == null) {
        if (file.isDirectory()) {
          mediaType = MIME_DIR;
        } else {
          String ext = getExtension(name()).toLowerCase(ENGLISH);
          String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
          mediaType = mime == null ? "application/octet-stream" : mime;
        }
      }
      return mediaType;
    }

    long length() {
      if (length == NONE) length = file.length();
      return length;
    }

    long modified() {
      if (lastModified == NONE) lastModified = file.lastModified();
      return lastModified;
    }

    int readable() {
      if (canRead == NONE) canRead = file.canRead() ? 1 : 0;
      return canRead;
    }

    int writable() {
      if (canWrite == NONE) canWrite = file.canWrite() ? 1 : 0;
      return canWrite;
    }
  }
}
