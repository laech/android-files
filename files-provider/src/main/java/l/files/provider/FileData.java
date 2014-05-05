package l.files.provider;

import android.webkit.MimeTypeMap;

import com.google.common.base.Predicate;

import java.io.File;

import l.files.io.file.Path;
import l.files.os.ErrnoException;
import l.files.os.Stat;
import l.files.os.Unistd;

import static com.google.common.base.Predicates.not;
import static java.util.Locale.ENGLISH;
import static l.files.common.database.DataTypes.booleanToInt;
import static l.files.common.database.DataTypes.intToBoolean;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
import static l.files.provider.FilesContract.getFileLocation;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

final class FileData {

  static final Predicate<FileData> HIDDEN = new Predicate<FileData>() {
    @Override public boolean apply(FileData input) {
      return intToBoolean(input.hidden);
    }
  };

  static final Predicate<FileData> NOT_HIDDEN = not(HIDDEN);

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

  public static FileData stat(Path path) throws ErrnoException {
    Stat stat = Stat.stat(path.toString());
    return new FileData(stat, path.toString(), path.name());
  }

  private int access(String path, int mode) {
    try {
      return booleanToInt(Unistd.access(path, mode));
    } catch (ErrnoException e) {
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

  @Override public String toString() {
    return reflectionToString(this, SHORT_PREFIX_STYLE);
  }
}
