package l.files.service;

import java.io.IOException;

/**
 * Exception thrown when trying to move/copy a directory into its own sub
 * directory.
 */
class CannotPasteIntoSelfException extends IOException {

  public CannotPasteIntoSelfException(String detailMessage) {
    super(detailMessage);
  }
}
