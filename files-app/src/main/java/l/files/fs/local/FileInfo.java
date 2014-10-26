package l.files.fs.local;

import android.webkit.MimeTypeMap;

import com.google.auto.value.AutoValue;
import com.google.common.net.MediaType;

import org.joda.time.Instant;

import java.io.File;
import java.io.IOException;

import l.files.fs.FileId;
import l.files.fs.FileStatus;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Information regarding a file at a point in time.
 * <h3>
 * Difference between this class and {@link File}
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
public abstract class FileInfo implements FileStatus {

  private FileId id;
  private String name;
  private MediaType basicMediaType;
  private Boolean readable;
  private Boolean writable;
  private Instant lastModifiedTime;

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

  @Override public FileId id() {
    if (id == null) {
      id = FileId.of(toFile());
    }
    return id;
  }

  @Override public String name() {
    if (name == null) {
      name = toFile().getName();
    }
    return name;
  }

  @Override public Instant lastModifiedTime() {
    if (lastModifiedTime == null) {
      lastModifiedTime = new Instant(modified());
    }
    return lastModifiedTime;
  }

  @Override public MediaType basicMediaType() {
    if (basicMediaType == null) {
      boolean dir;
      if (isSymbolicLink()) {
        // java.io.File will us about the actual file being linked to
        dir = toFile().isDirectory();
      } else {
        dir = isDirectory();
      }
      if (dir) {
        basicMediaType = MediaType.create("application", "x-directory");
      } else {
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        String ext = getExtension(name());
        String typeString = typeMap.getMimeTypeFromExtension(ext);
        if (typeString != null) {
          basicMediaType = MediaType.parse(typeString);
        } else {
          basicMediaType = MediaType.OCTET_STREAM;
        }
      }
    }

    return basicMediaType;
  }

  /**
   * Gets the media type of this file based on its file extension or type.
   */
  public String mime() {
    return basicMediaType().toString();
  }

  @Override public boolean isReadable() {
    if (readable == null) {
      readable = access(Unistd.R_OK);
    }
    return readable;
  }

  @Override public boolean isWritable() {
    if (writable == null) {
      writable = access(Unistd.W_OK);
    }
    return writable;
  }

  @Override public boolean isExecutable() {
    return false;
  }

  private boolean access(int mode) {
    try {
      Unistd.access(path(), mode);
      return true;
    } catch (ErrnoException e) {
      return false;
    }
  }

  @Override public boolean isHidden() {
    return name().startsWith(".");
  }

  /**
   * Gets the ID of the device containing this file.
   */
  public long device() {
    return stat().dev();
  }

  /**
   * File serial number (inode) of this file.
   */
  public long inode() {
    return stat().ino();
  }

  /**
   * Size of this file in bytes.
   */
  @Override public long size() {
    return stat().size();
  }

  /**
   * Last modified time of this file in milliseconds.
   */
  public long modified() {
    return stat().mtime() * 1000L;
  }

  @Override public boolean isSymbolicLink() {
    return Stat.S_ISLNK(stat().mode());
  }

  @Override public boolean isRegularFile() {
    return Stat.S_ISREG(stat().mode());
  }

  @Override public boolean isDirectory() {
    return Stat.S_ISDIR(stat().mode());
  }

  public File toFile() {
    return new File(path());
  }
}
