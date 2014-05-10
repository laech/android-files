package l.files.io.file.operations;

import java.io.File;
import java.io.IOException;

/**
 * An IOException indicating there was a problem operating on the specified
 * file.
 */
public class FileException extends IOException {

  private final File file;

  public FileException(File file) {
    this.file = file;
  }

  public File file() {
    return file;
  }
}
