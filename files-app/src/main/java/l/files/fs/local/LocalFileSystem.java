package l.files.fs.local;

import l.files.fs.Path;
import l.files.fs.FileSystem;

public class LocalFileSystem extends FileSystem {

  private static final LocalFileSystem INSTANCE = new LocalFileSystem();

  public static LocalFileSystem get() {
    return INSTANCE;
  }

  private LocalFileSystem() {}

  public static boolean canHandle(Path path) {
    try {
      LocalPath.check(path);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override public LocalFileStatus stat(Path path, boolean followLink) {
    return LocalFileStatus.stat(path, followLink);
  }

  @Override public void symlink(Path target, Path link) {
    LocalPath.check(target);
    LocalPath.check(link);
    try {
      Unistd.symlink(target.toString(), link.toString());
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
  }
}
