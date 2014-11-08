package l.files.fs;

public final class Files {
  private Files() {}

  /**
   * @see FileSystem#stat(Path, boolean)
   */
  public static FileStatus stat(Path path, boolean followLink) {
    return FileSystem.get(path).stat(path, followLink);
  }

  /**
   * @see FileSystem#symlink(Path, Path).
   */
  public static void symlink(Path target, Path link) {
    FileSystem.get(target).symlink(target, link);
  }
}
