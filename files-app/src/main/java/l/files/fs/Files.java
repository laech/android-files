package l.files.fs;

public final class Files {
  private Files() {}

  /**
   * @see FileSystem#stat(FileId, LinkOption)
   */
  public static FileStatus stat(FileId file, LinkOption option) {
    return FileSystem.get(file).stat(file, option);
  }

  /**
   * @see FileSystem#symlink(FileId, FileId).
   */
  public static void symlink(FileId target, FileId link) {
    FileSystem.get(target).symlink(target, link);
  }
}
