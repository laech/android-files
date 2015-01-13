package l.files.fs;

import java.net.URI;

import l.files.fs.local.LocalFileSystem;

public final class DefaultFileSystemProvider implements FileSystemProvider {

  private static final DefaultFileSystemProvider INSTANCE =
      new DefaultFileSystemProvider();

  private DefaultFileSystemProvider() {}

  public static DefaultFileSystemProvider get() {
    return INSTANCE;
  }

  @Override public FileSystem get(URI uri) {
    if (!"file".equals(uri.getScheme())) {
      throw new NoSuchFileSystemException(uri.toString());
    }
    return LocalFileSystem.get();
  }
}
