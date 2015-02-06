package l.files.operations;

import java.io.IOException;

import l.files.fs.NoSuchFileException;
import l.files.fs.Path;
import l.files.fs.PathEntry;
import l.files.fs.local.LocalResourceStatus;
import l.files.fs.local.LocalFileVisitor;
import l.files.fs.local.LocalPath;

import static l.files.fs.local.Files.remove;

public final class Delete extends AbstractOperation {

  private volatile int deletedItemCount;
  private volatile long deletedByteCount;

  public Delete(Iterable<? extends Path> paths) {
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

  @Override void process(Path path, FailureRecorder listener)
      throws InterruptedException {
    deleteTree(path, listener);
  }

  private void deleteTree(Path path, FailureRecorder listener)
      throws InterruptedException {
    try {
      for (PathEntry entry : LocalFileVisitor.get().postOrderTraversal(path)) {
        checkInterrupt();
        try {
          delete(entry.getPath());
        } catch (NoSuchFileException e) {
          // Ignore
        } catch (IOException e) {
          listener.onFailure(entry.getPath(), e);
        }
      }
    } catch (IOException e) {
      listener.onFailure(path, e);
    }
  }

  private void delete(Path path) throws IOException {
    long size = LocalResourceStatus.stat(path, false).getSize();
    remove(LocalPath.check(path).getFile().getPath());
    deletedByteCount += size;
    deletedItemCount++;
  }
}
