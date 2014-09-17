package l.files.io.file;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.google.auto.value.AutoValue;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.Stat.S_ISBLK;
import static l.files.io.file.Stat.S_ISCHR;
import static l.files.io.file.Stat.S_ISDIR;
import static l.files.io.file.Stat.S_ISFIFO;
import static l.files.io.file.Stat.S_ISLNK;
import static l.files.io.file.Stat.S_ISREG;
import static l.files.io.file.Stat.S_ISSOCK;
import static l.files.io.file.Unistd.R_OK;
import static l.files.io.file.Unistd.W_OK;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Information regarding a file at a point in time.
 * <h3>
 * Difference between this class and {@link java.io.File}
 * </h3>
 * {@link File} represents a handle to the underlying file,
 * methods such as {@link File#lastModified()} will always return the
 * latest value by querying the underlying file.
 * <p/>
 * {@link FileInfo} is mostly a snapshot of the file information, therefore if
 * the underlying file changes after an instance of this class is created, the
 * change won't be reflected by the existing instance.
 */
@AutoValue
public abstract class FileInfo {

  private String uri;
  private String name;
  private String mime;
  private Boolean readable;
  private Boolean writable;

  FileInfo() {}

  public abstract String path();

  abstract Stat stat();

  /**
   * @throws IOException includes path is not accessible or doesn't exist
   */
  public static FileInfo read(String parent, String child) throws IOException {
    checkNotNull(parent, "parent");
    checkNotNull(child, "child");
    String path = concat(parent, child);
    return read(path);
  }

  /**
   * @throws IOException includes path is not accessible or doesn't exist
   */
  public static FileInfo read(String path) throws IOException {
    checkNotNull(path, "path");
    Stat stat = Stat.lstat(path);
    return new AutoValue_FileInfo(path, stat);
  }

  public String name() {
    if (name == null) {
      name = new File(path()).getName();
    }
    return name;
  }

  /**
   * Gets the media type of this file based on its file extension or type.
   */
  public String mime() {
    if (mime == null) {
      boolean dir;
      if (isSymbolicLink()) {
        // java.io.File will us about the actual file being linked to
        dir = new File(path()).isDirectory();
      } else {
        dir = isDirectory();
      }
      if (dir) {
        mime = "application/x-directory";
      } else {
        mime = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(getExtension(name()));
      }
      if (mime == null) {
        mime = "application/octet-stream";
      }
    }
    return mime;
  }

  public String uri() {
    if (uri == null) {
      uri = Uri.fromFile(new File(path())).toString();
    }
    return uri;
  }

  public boolean isReadable() {
    if (readable == null) {
      readable = access(R_OK);
    }
    return readable;
  }

  public boolean isWritable() {
    if (writable == null) {
      writable = access(W_OK);
    }
    return writable;
  }

  private boolean access(int mode) {
    try {
      Unistd.access(path(), mode);
      return true;
    } catch (ErrnoException e) {
      return false;
    }
  }

  public boolean isHidden() {
    return name().startsWith(".");
  }

  /**
   * Gets the ID of the device containing this file.
   */
  public long device() {
    return stat().dev;
  }

  /**
   * File serial number (inode) of this file.
   */
  public long inode() {
    return stat().ino;
  }

  /**
   * Size of this file in bytes.
   */
  public long size() {
    return stat().size;
  }

  /**
   * Last modified time of this file in milliseconds.
   */
  public long modified() {
    return stat().mtime * 1000L;
  }

  public boolean isSocket() {
    return S_ISSOCK(stat().mode);
  }

  public boolean isSymbolicLink() {
    return S_ISLNK(stat().mode);
  }

  public boolean isRegularFile() {
    return S_ISREG(stat().mode);
  }

  public boolean isBlockDevice() {
    return S_ISBLK(stat().mode);
  }

  public boolean isDirectory() {
    return S_ISDIR(stat().mode);
  }

  public boolean isCharacterDevice() {
    return S_ISCHR(stat().mode);
  }

  public boolean isFifo() {
    return S_ISFIFO(stat().mode);
  }

  public File toFile() {
    return new File(path());
  }
}
