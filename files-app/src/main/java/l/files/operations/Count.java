package l.files.operations;

import java.io.IOException;

import l.files.fs.FileSystemException;
import l.files.fs.Path;
import l.files.fs.PathEntry;
import l.files.fs.local.LocalFileVisitor;

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
    for (PathEntry entry : LocalFileVisitor.get().breadthFirstTraversal(path)) {
      checkInterrupt();
      count++;
      onCount(entry.path());
    }
  }

  void onCount(Path path) {
  }
}
