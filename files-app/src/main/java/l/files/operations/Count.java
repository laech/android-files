package l.files.operations;

import l.files.fs.local.DirectoryTreeTraverser;

import static l.files.fs.local.DirectoryTreeTraverser.Entry;

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
    count(path);
  }

  private void count(String path) throws InterruptedException {
    Entry root = Entry.create(path);
    for (Entry entry : DirectoryTreeTraverser.get().breadthFirstTraversal(root)) {
      checkInterrupt();
      count++;
      onCount(entry.path());
    }
  }

  void onCount(String path) {
  }
}
