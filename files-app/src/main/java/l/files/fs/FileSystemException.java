package l.files.fs;

/**
 * Root class for all file system related exceptions.
 */
public class FileSystemException extends RuntimeException {

  public FileSystemException(String detailMessage) {
    super(detailMessage);
  }

  public FileSystemException(Throwable throwable) {
    super(throwable);
  }

}
