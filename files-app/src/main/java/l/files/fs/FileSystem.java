package l.files.fs;

import l.files.fs.local.LocalFileSystem;

/**
 * A virtual file system interface provides operations on files.
 */
public abstract class FileSystem {

  /**
   * Gets the file system that is able to handle the given file.
   * Currently only the local file system is supported.
   *
   * @throws NoSuchFileSystemException if no file system can be found to
   *                                   handle the given type of file
   */
  public static FileSystem get(FileId id) {
    if (id.scheme().equals(LocalFileSystem.SCHEME)) {
      return LocalFileSystem.get();
    }
    throw new NoSuchFileSystemException(id.toString());
  }

  /**
   * Gets the URI scheme this file system handles.
   */
  public abstract Scheme scheme();

  /**
   * Reads the status of a file.
   *
   * @throws FileSystemException      if failed to read file status
   * @throws IllegalArgumentException if file scheme is known to this instance
   */
  public abstract FileStatus stat(FileId file, LinkOption option);

  /**
   * Creates a symbolic link.
   *
   * @param target the target of the symbolic link
   * @param link   the link to create
   * @throws FileSystemException      if failed to create the link
   * @throws IllegalArgumentException if file scheme is known to this instance
   */
  public abstract void symlink(FileId target, FileId link);
}
