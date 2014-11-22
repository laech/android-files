package l.files.operations;

import java.io.IOException;

import l.files.fs.NoSuchFileException;
import l.files.fs.Path;
import l.files.fs.local.LocalDirectoryTreeTraverser;
import l.files.fs.local.LocalFileStatus;
import l.files.fs.local.LocalPath;

import static l.files.fs.local.Files.remove;
import static l.files.fs.local.LocalDirectoryTreeTraverser.Entry;

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
    // TODO fix this catch FileSystemException
    for (Entry entry : LocalDirectoryTreeTraverser.get().postOrderTraversal(LocalPath.of(path))) {
      checkInterrupt();
      try {
        delete(entry.path());
      } catch (NoSuchFileException e) {
        // Ignore
      } catch (IOException e) {
        listener.onFailure(entry.path().toString(), e);
      }
    }
  }

  private void delete(Path path) throws IOException {
    long size = LocalFileStatus.stat(path, false).size();
    remove(LocalPath.check(path).toFile().getPath());
    deletedByteCount += size;
    deletedItemCount++;
  }
}
