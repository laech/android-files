package l.files.fs;

import l.files.fs.local.LocalFileSystem;

final class FileSystems {
  private FileSystems() {}

  /**
   * Gets the file system that is able to handle the given path.
   * Currently only the local file system is supported.
   *
   * @throws NoSuchFileSystemException if no file system can be found to
   *                                   handle the given type of path
   */
  static FileSystem get(Path path) {
    return get(path.toUri().getScheme());
  }

  /**
   * Gets the file system that is able to handle the URI scheme.
   * Currently only the local file system is supported.
   *
   * @throws NoSuchFileSystemException if no file system can be found to
   *                                   handle the given scheme
   */
  static FileSystem get(String scheme) {
    if ("file".equals(scheme)) {
      return LocalFileSystem.get();
    }
    throw new NoSuchFileSystemException(scheme);
  }

}
