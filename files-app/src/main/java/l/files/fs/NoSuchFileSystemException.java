package l.files.fs;

/**
 * Indicates a file system is not found for the given file type
 */
public class NoSuchFileSystemException extends FileSystemException {

  public NoSuchFileSystemException(String detailMessage) {
    super(detailMessage);
  }
}
