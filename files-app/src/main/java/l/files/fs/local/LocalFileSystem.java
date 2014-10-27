package l.files.fs.local;

import l.files.fs.FileId;
import l.files.fs.FileSystem;
import l.files.fs.LinkOption;
import l.files.fs.Scheme;

public class LocalFileSystem extends FileSystem {

  /**
   * The URI scheme this file system handles.
   */
  public static final Scheme SCHEME = Scheme.parse("file");

  private static final LocalFileSystem INSTANCE = new LocalFileSystem();

  public static LocalFileSystem get() {
    return INSTANCE;
  }

  private LocalFileSystem() {}

  @Override public Scheme scheme() {
    return SCHEME;
  }

  @Override public LocalFileStatus stat(FileId file, LinkOption option) {
    checkScheme(file);
    try {
      return LocalFileStatus.read(file, option);
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
  }

  @Override public void symlink(FileId target, FileId link) {
    checkScheme(target);
    checkScheme(link);
    try {
      Unistd.symlink(target.toUri().getPath(), link.toUri().getPath());
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
  }

  private void checkScheme(FileId file) {
    if (!scheme().equals(file.scheme())) {
      throw new IllegalArgumentException(file.toString());
    }
  }
}
