package l.files.fs.local;

import com.google.auto.value.AutoValue;
import com.google.common.net.MediaType;

import org.joda.time.Instant;

import java.io.File;

import l.files.fs.FileStatus;
import l.files.fs.FileSystemException;
import l.files.fs.FileTypeDetector;
import l.files.fs.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static l.files.fs.local.Stat.S_ISBLK;
import static l.files.fs.local.Stat.S_ISCHR;
import static l.files.fs.local.Stat.S_ISDIR;
import static l.files.fs.local.Stat.S_ISFIFO;
import static l.files.fs.local.Stat.S_ISLNK;
import static l.files.fs.local.Stat.S_ISREG;
import static l.files.fs.local.Stat.S_ISSOCK;

@AutoValue
public abstract class LocalFileStatus implements FileStatus {

  private String name;
  private MediaType basicMediaType;
  private Boolean readable;
  private Boolean writable;
  private Instant lastModifiedTime;

  LocalFileStatus() {}

  @Override public abstract LocalPath path();

  abstract Stat stat();

  @Deprecated
  public static LocalFileStatus read(String parent, String child) {
    checkNotNull(parent, "parent");
    checkNotNull(child, "child");
    return stat(new File(parent, child), false);
  }

  @Deprecated
  public static LocalFileStatus read(String path) {
    checkNotNull(path, "path");
    try {
      Stat stat = Stat.lstat(path);
      return new AutoValue_LocalFileStatus(LocalPath.of(path), stat);
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
  }

  /**
   * @throws FileSystemException if failed to get status
   */
  static LocalFileStatus stat(File file, boolean followLink) {
    return stat(LocalPath.of(file), followLink);
  }

  /**
   * @throws FileSystemException      if failed to get status
   * @throws IllegalArgumentException if the path is not of supported type
   */
  public static LocalFileStatus stat(Path path, boolean followLink) {
    LocalPath.check(path);
    final Stat stat;
    try {
      if (followLink) {
        stat = Stat.stat(path.toString());
      } else {
        stat = Stat.lstat(path.toString());
      }
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
    return new AutoValue_LocalFileStatus((LocalPath) path, stat);
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
      try {
        FileTypeDetector detector = BasicFileTypeDetector.get();
        basicMediaType = detector.detect(path(), true);
      } catch (FileSystemException e) {
        basicMediaType = OCTET_STREAM;
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
      Unistd.access(path().toString(), mode);
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
    return S_ISLNK(stat().mode());
  }

  @Override public boolean isRegularFile() {
    return S_ISREG(stat().mode());
  }

  @Override public boolean isDirectory() {
    return S_ISDIR(stat().mode());
  }

  public boolean isSocket() {
    return S_ISSOCK(stat().mode());
  }

  public boolean isBlockDevice() {
    return S_ISBLK(stat().mode());
  }

  public boolean isCharacterDevice() {
    return S_ISCHR(stat().mode());
  }

  public boolean isFifo() {
    return S_ISFIFO(stat().mode());
  }

  public File toFile() {
    return path().file();
  }
}
