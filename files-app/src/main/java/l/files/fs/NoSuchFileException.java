package l.files.fs;

/**
 * Indicates the target file does not exist on the file system.
 */
public class NoSuchFileException extends FileSystemException {

  public NoSuchFileException(Throwable throwable) {
    super(throwable);
  }
}
