package l.files.service;

import java.io.File;
import java.io.IOException;

class RestrictedException extends IOException {

  private final File directory;

  RestrictedException(File directory) {
    this.directory = directory;
  }
}
