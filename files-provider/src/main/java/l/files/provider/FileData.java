package l.files.provider;

import android.webkit.MimeTypeMap;

import java.io.File;

import l.files.os.OsException;
import l.files.os.Stat;
import l.files.os.Unistd;

import static java.util.Locale.ENGLISH;
import static l.files.common.database.DataTypes.booleanToInt;
import static l.files.common.database.DataTypes.intToBoolean;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
import static l.files.provider.FilesContract.getFileLocation;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

final class FileData {

  final long lastModified;
  final long length;
  final int directory;
  final int hidden;
  final int canRead;
  final int canWrite;
  final String name;
  final String path;
  final String location;
  final String mime;

  /*
   * Should not be accessing the file system from the constructor, but given the
   * number of final fields, doing this here is more maintainable. So the
   * constructor is private, and a public static method is used for creating an
   * instance, this hides away the fact that this is done in the constructor,
   * also gives the possibility of changing this in the future.
   */

  private FileData(File file) {
    this.lastModified = file.lastModified();
    this.length = file.length();
    this.directory = booleanToInt(file.isDirectory());
    this.canRead = booleanToInt(file.canRead());
    this.canWrite = booleanToInt(file.canWrite());
    this.name = file.getName();
    this.path = file.getPath();
    this.location = getFileLocation(file);
    this.mime = mime(file, name);
    this.hidden = booleanToInt(name.startsWith("."));
  }

  private FileData(Stat stat, String path, String name) {
    this.lastModified = stat.mtime * 1000L;
    this.length = stat.size;
    this.directory = booleanToInt(Stat.S_ISDIR(stat.mode));
    this.canRead = access(path, Unistd.R_OK);
    this.canWrite = access(path, Unistd.W_OK);
    this.name = name;
    this.path = path;
    this.location = getFileLocation(new File(path));
    this.mime = mime(name, intToBoolean(this.directory));
    this.hidden = booleanToInt(name.startsWith("."));
  }

  private int access(String path, int mode) {
    try {
      return booleanToInt(Unistd.access(path, mode));
    } catch (OsException e) {
      return booleanToInt(false);
    }
  }

  private static String mime(String name, boolean dir) {
    if (dir) {
      return MIME_DIR;
    }
    String ext = getExtension(name).toLowerCase(ENGLISH);
    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    return mime == null ? "application/octet-stream" : mime;
  }

  private static String mime(File file, String name) {
    if (file.isDirectory()) {
      return MIME_DIR;
    }
    String ext = getExtension(name).toLowerCase(ENGLISH);
    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    return mime == null ? "application/octet-stream" : mime;
  }

  public static FileData from(File file) {
    return new FileData(file);
  }

  public static FileData from(Stat stat, String path, String name) {
    return new FileData(stat, path, name);
  }

  /**
   * Converts the given files.
   * <p/>
   * Only use this for small number of files, as reading properties from files
   * are expensive and slow.
   */
  public static FileData[] from(File[] files) {
    FileData[] data = new FileData[files.length];
    for (int i = 0; i < files.length; i++) {
      data[i] = new FileData(files[i]);
    }
    return data;
  }

  @Override public int hashCode() {
    return reflectionHashCode(this);
  }

  @Override public boolean equals(Object o) {
    return reflectionEquals(this, o);
  }

  @Override public String toString() {
    return reflectionToString(this, SHORT_PREFIX_STYLE);
  }
}
