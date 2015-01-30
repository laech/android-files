package l.files.fs.local;

import java.io.File;
import java.net.URI;

import l.files.fs.DirectoryStream;
import l.files.fs.FileSystem;
import l.files.fs.FileSystemException;
import l.files.fs.FileVisitor;
import l.files.fs.Path;
import l.files.fs.WatchService;

public class LocalFileSystem implements FileSystem {

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

  @Override public Path path(URI uri) {
    return LocalPath.of(new File(uri));
  }

  @Override public Path path(String path) {
    return LocalPath.of(new File(path));
  }

  @Override public LocalFileStatus stat(Path path, boolean followLink) throws FileSystemException {
    return LocalFileStatus.stat(path, followLink);
  }

  @Override public void symlink(Path target, Path link) throws FileSystemException {
    LocalPath.check(target);
    LocalPath.check(link);
    try {
      Unistd.symlink(target.toString(), link.toString());
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
  }

  @Override public DirectoryStream openDirectory(Path path) throws FileSystemException {
    return LocalDirectoryStream.open(path);
  }

  @Override public FileVisitor visitor() {
    return LocalFileVisitor.get();
  }

  @Override public WatchService watcher() {
    return LocalWatchService.get();
  }
}
