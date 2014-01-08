package l.files.service;

import java.io.File;
import java.io.IOException;

/**
 * An IOException indicating there was a problem operating on the specified
 * file.
 */
class FileException extends IOException {

  private final File file;

  FileException(File file) {
    this.file = file;
  }

  public File file() {
    return file;
  }
}
