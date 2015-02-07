package l.files.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kotlin.Function2;
import kotlin.Unit;
import l.files.fs.Path;
import l.files.fs.Resource;

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

  static final class FailureRecorder {
    private final List<Failure> failures;
    private final int limit;

    FailureRecorder(int limit) {
      this.limit = limit;
      this.failures = new ArrayList<>();
    }

    public void onFailure(Path path, IOException failure)
        throws FileException {
      if (failures.size() > limit) {
        throw new FileException(failures);
      }
      failures.add(Failure.create(path.toString(), failure));
    }

    void throwIfNotEmpty() {
      FileException.throwIfNotEmpty(failures);
    }
  }

  protected static final class ErrorCollector implements Function2<Resource, IOException, Unit> {
    private final FailureRecorder recorder;

    ErrorCollector(FailureRecorder recorder) {
      this.recorder = recorder;
    }

    @Override public Unit invoke(Resource resource, IOException e) {
      recorder.onFailure(resource.getPath(), e);
      return null;
    }
  }

}
