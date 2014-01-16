package l.files.provider;

import android.webkit.MimeTypeMap;

import com.google.common.primitives.Longs;

import java.io.File;
import java.util.Comparator;

import static java.util.Locale.ENGLISH;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.MIME_DIR;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class FileData {

  /**
   * Conforms to the spec of {@link FileInfo#SORT_BY_NAME}.
   */
  public static final Comparator<FileData> NAME_COMPARATOR =
      new Comparator<FileData>() {
        @Override public int compare(FileData a, FileData b) {
          return a.name.compareToIgnoreCase(b.name);
        }
      };

  /**
   * Conforms to the spec of {@link FileInfo#SORT_BY_MODIFIED}.
   */
  public static final Comparator<FileData> LAST_MODIFIED_COMPARATOR_REVERSE =
      new Comparator<FileData>() {
        @Override public int compare(FileData a, FileData b) {
          return Longs.compare(a.lastModified, b.lastModified) * -1;
        }
      };

  /**
   * Conforms to the spec of {@link FileInfo#SORT_BY_SIZE}.
   */
  public static final Comparator<FileData> SIZE_COMPARATOR_REVERSE =
      new Comparator<FileData>() {
        @Override public int compare(FileData a, FileData b) {
          if (a.directory && b.directory) return NAME_COMPARATOR.compare(a, b);
          if (a.directory) return 1;
          if (b.directory) return -1;
          return Longs.compare(a.length, b.length) * -1;
        }
      };

  final long lastModified;
  final long length;
  final boolean directory;
  final int canRead;
  final int canWrite;
  final String name;
  final String path;
  final String uri;
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
    this.directory = file.isDirectory();
    this.canRead = file.canRead() ? 1 : 0;
    this.canWrite = file.canWrite() ? 1 : 0;
    this.name = file.getName();
    this.path = file.getPath();
    this.uri = file.toURI().toString();
    this.mime = mime(file, name);
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
}
