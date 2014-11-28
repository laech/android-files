package l.files.fs;

public final class Files {
  private Files() {}

  /**
   * @see FileSystem#stat(Path, boolean)
   */
  public static FileStatus stat(Path path, boolean followLink) {
    return FileSystems.get(path).stat(path, followLink);
  }

  /**
   * @see FileSystem#symlink(Path, Path).
   */
  public static void symlink(Path target, Path link) {
    FileSystems.get(target).symlink(target, link);
  }

  /**
   * @see FileSystem#openDirectory(Path)
   */
  public static DirectoryStream openDirectory(Path path) {
    return FileSystems.get(path).openDirectory(path);
  }
}
