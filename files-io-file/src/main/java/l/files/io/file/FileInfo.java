package l.files.io.file;

import com.google.auto.value.AutoValue;

import java.io.IOException;

import l.files.io.os.Stat;

import static l.files.io.os.Stat.S_ISBLK;
import static l.files.io.os.Stat.S_ISCHR;
import static l.files.io.os.Stat.S_ISDIR;
import static l.files.io.os.Stat.S_ISFIFO;
import static l.files.io.os.Stat.S_ISLNK;
import static l.files.io.os.Stat.S_ISREG;
import static l.files.io.os.Stat.S_ISSOCK;
import static org.apache.commons.io.FilenameUtils.concat;

/**
 * Information regarding a file at a point in time. Changes made to the file
 * after the creation of a {@link FileInfo} instance will not be reflected.
 */
@AutoValue
public abstract class FileInfo {
  FileInfo() {}

  public abstract String path();

  abstract Stat stat();

  /**
   * @throws IOException includes path is not accessible or doesn't exist
   */
  public static FileInfo get(String parent, String child) throws IOException {
    String path = concat(parent, child);
    return get(path);
  }

  /**
   * @throws IOException includes path is not accessible or doesn't exist
   */
  public static FileInfo get(String path) throws IOException {
    Stat stat = Stat.lstat(path);
    return new AutoValue_FileInfo(path, stat);
  }

  /**
   * Gets the ID of the device containing this file.
   */
  public long dev() {
    return stat().dev;
  }

  /**
   * File serial number (inode) of this file.
   */
  public long ino() {
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
  public long lastModified() {
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
}
