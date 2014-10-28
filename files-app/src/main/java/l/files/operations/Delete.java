package l.files.operations;

import java.io.IOException;

import l.files.fs.local.LocalDirectoryTreeTraverser;
import l.files.fs.local.LocalFileStatus;

import static l.files.fs.local.LocalDirectoryTreeTraverser.Entry;
import static l.files.fs.local.Files.remove;

public final class Delete extends AbstractOperation {

  private volatile int deletedItemCount;
  private volatile long deletedByteCount;

  public Delete(Iterable<String> paths) {
    super(paths);
  }

  /**
   * Gets the number of items deleted so far.
   */
  public int getDeletedItemCount() {
    return deletedItemCount;
  }

  /**
   * Gets the number of bytes deleted so far.
   */
  public long getDeletedByteCount() {
    return deletedByteCount;
  }

  @Override void process(String path, FailureRecorder listener)
      throws InterruptedException {
    deleteTree(path, listener);
  }

  private void deleteTree(String path, FailureRecorder listener)
      throws InterruptedException {
    Entry root = Entry.create(path);
    for (Entry entry : LocalDirectoryTreeTraverser.get().postOrderTraversal(root)) {
      checkInterrupt();
      try {
        delete(entry.path());
      } catch (IOException e) {
        listener.onFailure(entry.path(), e);
      }
    }
  }

  private void delete(String path) throws IOException {
    long size = LocalFileStatus.read(path).size();
    remove(path);
    deletedByteCount += size;
    deletedItemCount++;
  }
}
