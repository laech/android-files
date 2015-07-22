package l.files.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Indicates one or more failures occurred during a {@link FileOperation}.
 */
final class FileException extends RuntimeException {

  private final List<Failure> failures;

  FileException(Collection<Failure> failures) {
    this.failures = unmodifiableList(new ArrayList<>(failures));
    if (this.failures.isEmpty()) {
      throw new IllegalArgumentException();
    }

    for (Failure failure : failures) {
      addSuppressed(failure.getCause());
    }
  }

  public static void throwIfNotEmpty(Collection<Failure> failures)
      throws FileException {
    if (!failures.isEmpty()) {
      throw new FileException(failures);
    }
  }

  public List<Failure> failures() {
    return failures;
  }
}
