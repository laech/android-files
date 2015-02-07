package l.files.operations;

import com.google.common.collect.ImmutableSet;

import l.files.fs.Path;

abstract class AbstractOperation implements FileOperation {

  /**
   * The amount of errors to catch before stopping. Don't want to hold an
   * endless amount of errors (resulting in OutOfMemoryError). And there is not
   * much point of continuing if number of errors reached this amount.
   */
  private static final int ERROR_LIMIT = 20;

  private final Iterable<Path> paths;

  AbstractOperation(Iterable<? extends Path> paths) {
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
    for (Path path : paths) {
      checkInterrupt();
      process(path, listener);
    }
    listener.throwIfNotEmpty();
  }

  abstract void process(Path path, FailureRecorder listener)
      throws InterruptedException;

}
