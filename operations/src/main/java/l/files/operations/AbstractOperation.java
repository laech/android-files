package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractOperation implements FileOperation {

  /**
   * The amount of errors to catch before stopping. Don't want to hold an
   * endless amount of errors (resulting in OutOfMemoryError). And there is not
   * much point of continuing if number of errors reached this amount.
   */
  private static final int ERROR_LIMIT = 20;

  private final Iterable<String> paths;

  AbstractOperation(Iterable<String> paths) {
    this.paths = ImmutableSet.copyOf(paths);
  }

  void checkInterrupt() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
  }

  @Override
  public void execute() throws InterruptedException {
    FailureRecorder listener = new FailureRecorder(ERROR_LIMIT);
    for (String path : paths) {
      checkInterrupt();
      process(path, listener);
    }
    listener.throwIfNotEmpty();
  }

  abstract void process(String path, FailureRecorder listener)
      throws InterruptedException;

  static final class FailureRecorder {
    private final List<Failure> failures;
    private final int limit;

    FailureRecorder(int limit) {
      this.limit = limit;
      this.failures = new ArrayList<>();
    }

    public void onFailure(String path, IOException failure)
        throws FileException {
      if (failures.size() > limit) {
        throw new FileException(failures);
      }
      failures.add(Failure.create(path, failure));
    }

    void throwIfNotEmpty() {
      FileException.throwIfNotEmpty(failures);
    }
  }
}
