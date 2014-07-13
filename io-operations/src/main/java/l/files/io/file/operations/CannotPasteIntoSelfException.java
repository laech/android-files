package l.files.io.file.operations;

/**
 * Exception thrown when trying to move/copy a directory into its own sub
 * directory.
 */
public class CannotPasteIntoSelfException extends IllegalArgumentException {

  public CannotPasteIntoSelfException(String detailMessage) {
    super(detailMessage);
  }
}
