package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import l.files.fs.Resource;
import l.files.fs.Visitor;

import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableSet;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

abstract class AbstractOperation implements FileOperation {

  /**
   * The amount of errors to catch before stopping. Don't want to hold an
   * endless amount of errors (resulting in OutOfMemoryError). And there is
   * not much point of continuing if number of errors reached this amount.
   */
  private static final int ERROR_LIMIT = 20;

  private final Iterable<Resource> resources;
  private final FailureRecorder recorder;

  AbstractOperation(Collection<? extends Resource> resources) {
    this.resources = unmodifiableSet(new HashSet<>(resources));
    this.recorder = new FailureRecorder(ERROR_LIMIT);
  }

  final void checkInterrupt() throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
  }

  final boolean isInterrupted() {
    return currentThread().isInterrupted();
  }

  final void record(Resource resource, final IOException exception) {
    recorder.onFailure(resource, exception);
  }

  final void traverse(Resource resource, OperationVisitor visitor) {
    try {
      resource.traverse(NOFOLLOW, visitor);
    } catch (IOException e) {
      record(resource, e);
    }
  }

  class OperationVisitor implements Visitor {

    @Override public Result onPreVisit(Resource res) throws IOException {
      return isInterrupted() ? TERMINATE : CONTINUE;
    }

    @Override public Result onPostVisit(Resource res) throws IOException {
      return isInterrupted() ? TERMINATE : CONTINUE;
    }

    @Override
    public void onException(Resource res, IOException e) throws IOException {
      record(res, e);
    }

  }

  @Override public void execute() throws InterruptedException {
    for (Resource resource : resources) {
      checkInterrupt();
      process(resource);
    }
    recorder.throwIfNotEmpty();
  }

  abstract void process(Resource resource) throws InterruptedException;

}
