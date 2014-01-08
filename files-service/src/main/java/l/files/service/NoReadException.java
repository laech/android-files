package l.files.service;

import java.io.File;

/**
 * A file or directory is not readable when attempting to read from it.
 */
public class NoReadException extends FileException {

  NoReadException(File file) {
    super(file);
  }
}
