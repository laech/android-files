package l.files.operations;

import java.io.IOException;

import l.files.fs.FileSystemException;
import l.files.fs.local.LocalDirectoryTreeTraverser;
import l.files.fs.local.LocalPath;

import static l.files.fs.local.LocalDirectoryTreeTraverser.Entry;

class Count extends AbstractOperation {

  private volatile int count;

  Count(Iterable<String> paths) {
    super(paths);
  }

  /**
   * Gets the number of items counted so far.
   */
  public int getCount() {
    return count;
  }

  @Override void process(String path, FailureRecorder listener)
      throws InterruptedException {
    try {
      count(path);
    } catch (FileSystemException e) {
      listener.onFailure(path, new IOException(e)); // TODO no IO wrapper
    }
  }

  private void count(String path) throws InterruptedException {
    for (Entry entry : LocalDirectoryTreeTraverser.get().breadthFirstTraversal(LocalPath.of(path))) {
      checkInterrupt();
      count++;
      onCount(entry.path().toString());
    }
  }

  void onCount(String path) {
  }
}
