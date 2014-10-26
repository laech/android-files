package l.files.fs;

import com.google.common.net.MediaType;

import org.joda.time.Instant;

/**
 * Contains static information regarding a file.
 * this status is created.
 */
public interface FileStatus {

  /**
   * The universally unique identifier of this file.
   */
  FileId id();

  String name();

  Instant lastModifiedTime();

  boolean isRegularFile();

  boolean isDirectory();

  boolean isSymbolicLink();

  /**
   * The size of this file in bytes.
   */
  long size();

  /**
   * True if the file is readable by the current user.
   */
  boolean isReadable();

  /**
   * True if the file is writable by the current user.
   */
  boolean isWritable();

  /**
   * True if the file is executable by the current user.
   */
  boolean isExecutable();

  /**
   * True if the file is considered hidden by the underlying file system.
   */
  boolean isHidden();

  /**
   * Gets the media type of this file based on its file extension or type,
   * without inspecting the content of the file.
   */
  MediaType basicMediaType();

}
