package l.files.io.file.operations;

import java.io.IOException;

/**
 * Exception thrown when trying to move/copy a directory into its own sub
 * directory.
 */
public class CannotPasteIntoSelfException extends IOException {

  public CannotPasteIntoSelfException(String detailMessage) {
    super(detailMessage);
  }
}
