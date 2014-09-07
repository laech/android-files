package l.files.operations;

/**
 * Exception thrown when trying to move/copy a directory into its own sub
 * directory.
 */
class CannotPasteIntoSelfException extends IllegalArgumentException {

  public CannotPasteIntoSelfException(String detailMessage) {
    super(detailMessage);
  }
}
