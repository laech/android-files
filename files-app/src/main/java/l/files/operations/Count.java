package l.files.operations;

import java.io.IOException;
import java.util.Iterator;

import kotlin.Function2;
import kotlin.Unit;
import l.files.fs.Path;
import l.files.fs.Resource;

import static l.files.fs.Resource.TraversalOrder.BREATH_FIRST;

class Count extends AbstractOperation {

  private volatile int count;

  Count(Iterable<? extends Path> paths) {
    super(paths);
  }

  /**
   * Gets the number of items counted so far.
   */
  public int getCount() {
    return count;
  }

  @Override void process(Path path, FailureRecorder listener) throws InterruptedException {
    try {
      count(path);
    } catch (IOException e) {
      listener.onFailure(path, new IOException(e)); // TODO no IO wrapper
    }
  }

  private void count(Path path) throws InterruptedException, IOException {
    Iterator<Resource> it = path.getResource().traverse(BREATH_FIRST, nullErrorHandler()).iterator();
    while (it.hasNext()) {
      checkInterrupt();
      Resource entry = it.next();
      count++;
      onCount(entry.getPath());
    }
  }

  private Function2<Resource, IOException, Unit> nullErrorHandler() {
    return new Function2<Resource, IOException, Unit>() {
      @Override public Unit invoke(Resource resource, IOException e) {
        return null;
      }
    };
  }

  void onCount(Path path) {
  }
}
