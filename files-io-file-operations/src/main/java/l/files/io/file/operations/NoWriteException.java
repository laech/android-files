package l.files.io.file.operations;

import java.io.File;

/**
 * A file or directory is not writable when attempting to write to it.
 */
public class NoWriteException extends FileException {

  public NoWriteException(File file) {
    super(file);
  }
}
