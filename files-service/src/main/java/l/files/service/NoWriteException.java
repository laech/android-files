package l.files.service;

import java.io.File;

/**
 * A file or directory is not writable when attempting to write to it.
 */
class NoWriteException extends FileException {

  NoWriteException(File file) {
    super(file);
  }
}
