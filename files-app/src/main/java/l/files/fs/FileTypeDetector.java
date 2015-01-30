package l.files.fs;

import com.google.common.net.MediaType;

public interface FileTypeDetector {

  /**
   * Detects the content type of a file.
   *
   * @throws FileSystemException      if failed to detect the file's media type
   * @throws IllegalArgumentException if the given file scheme can't be handled
   *                                  by this detector's file system
   */
  MediaType detect(Path path, boolean followLink) throws FileSystemException;

}
