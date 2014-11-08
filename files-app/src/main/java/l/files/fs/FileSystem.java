package l.files.fs;

import l.files.fs.local.LocalFileSystem;

/**
 * A virtual file system interface provides operations on files.
 */
public abstract class FileSystem {

  /**
   * Gets the file system that is able to handle the given path.
   * Currently only the local file system is supported.
   *
   * @throws NoSuchFileSystemException if no file system can be found to
   *                                   handle the given type of path
   */
  public static FileSystem get(Path path) {
    if (LocalFileSystem.canHandle(path)) {
      return LocalFileSystem.get();
    }
    throw new NoSuchFileSystemException(path.toString());
  }

  /**
   * Reads the status of a file.
   *
   * @param followLink if true return status of the target,
   *                   else return status of the link itself
   * @throws FileSystemException      if failed to read file status
   * @throws IllegalArgumentException if file scheme is known to this instance
   */
  public abstract FileStatus stat(Path path, boolean followLink);

  /**
   * Creates a symbolic link.
   *
   * @param target the target of the symbolic link
   * @param link   the link to create
   * @throws FileSystemException      if failed to create the link
   * @throws IllegalArgumentException if file scheme is known to this instance
   */
  public abstract void symlink(Path target, Path link);
}
