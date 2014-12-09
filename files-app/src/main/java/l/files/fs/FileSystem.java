package l.files.fs;

import java.net.URI;

/**
 * A virtual file system interface provides operations on files.
 */
public interface FileSystem {

  /**
   * Gets the path from the given URI.
   *
   * @throws IllegalArgumentException if the URI scheme is not supported
   */
  Path getPath(URI uri);

  /**
   * Reads the status of a file.
   *
   * @param followLink if true return status of the target,
   *                   else return status of the link itself
   * @throws FileSystemException      if failed to read file status
   * @throws IllegalArgumentException if file scheme is known to this instance
   */
  FileStatus stat(Path path, boolean followLink);

  /**
   * Creates a symbolic link.
   *
   * @param target the target of the symbolic link
   * @param link   the link to create
   * @throws FileSystemException      if failed to create the link
   * @throws IllegalArgumentException if file scheme is known to this instance
   */
  void symlink(Path target, Path link);

  /**
   * Opens a directory stream to iterate through the entries of the directory.
   *
   * @throws FileSystemException if failed to open the directory
   */
  DirectoryStream openDirectory(Path path);

  /**
   * Gets the shared watch service of this file system for monitoring file
   * system events.
   */
  WatchService getWatchService();
}
