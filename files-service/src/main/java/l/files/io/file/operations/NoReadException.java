package l.files.io.file.operations;

import java.io.File;

/**
 * A file or directory is not readable when attempting to read from it.
 */
public class NoReadException extends FileException {

  public NoReadException(File file) {
    super(file);
  }
}
