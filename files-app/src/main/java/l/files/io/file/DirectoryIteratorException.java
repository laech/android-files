package l.files.io.file;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DirectoryIteratorException extends RuntimeException {

  public DirectoryIteratorException(Exception cause) {
    initCause(checkNotNull(cause, "cause"));
  }
}
