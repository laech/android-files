package l.files.fs;

import java.net.URI;

public interface FileSystemProvider {

  /**
   * Gets the file system that can handle the given URI.
   *
   * @throws NoSuchFileSystemException if none
   */
  FileSystem get(URI uri);

}
