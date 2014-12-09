package l.files.fs;

import l.files.fs.local.LocalFileSystem;

public final class FileSystems {
  private FileSystems() {}

  /**
   * Gets the file system that is able to handle the given path.
   *
   * @throws NoSuchFileSystemException if no file system can be found to
   *                                   handle the given type of path
   */
  public static FileSystem get(Path path) {
    return get(path.toUri().getScheme());
  }

  /**
   * Gets the file system that is able to handle the URI scheme.
   *
   * @throws NoSuchFileSystemException if no file system can be found to
   *                                   handle the given scheme
   */
  public static FileSystem get(String scheme) {
    if ("file".equals(scheme)) {
      return LocalFileSystem.get();
    }
    throw new NoSuchFileSystemException(scheme);
  }

}
