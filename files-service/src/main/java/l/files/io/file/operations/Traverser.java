package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static l.files.io.file.operations.Operations.listDirectoryChildren;
import static org.apache.commons.io.FileUtils.isSymlink;

/**
 * Traverses a set of root files in a breath first manner. Ignores any
 * symlinks.
 */
public abstract class Traverser<V> implements Callable<V> {

  private final Set<File> roots;
  private final Cancellable listener;

  protected Traverser(Cancellable listener, Iterable<File> roots) {
    this.roots = ImmutableSet.copyOf(checkNotNull(roots, "roots"));
    this.listener = checkNotNull(listener, "listener");
  }

  @Override public final V call() throws IOException {
    Queue<File> queue = newLinkedList(roots);
    while (!queue.isEmpty()) {
      if (listener.isCancelled()) {
        return null;
      }

      File file = queue.poll();

      /*
       * Symlinks are not supported yet, e.g. there isn't a way with the current
       * Java API to delete symlinks without deleting the actual
       * files/directories they are linked to.
       */
      if (isSymlink(file)) {
        continue;
      }

      if (file.isDirectory()) {
        queue.addAll(listDirectoryChildren(file));
        onDirectory(file);
      } else {
        onFile(file);
      }
    }

    onFinish();

    return getResult();
  }

  /**
   * Notify the given directory encountered during breath first traversal.
   */
  protected void onDirectory(File dir) throws IOException {}

  /**
   * Notify the given file encountered during breath first traversal.
   */
  protected void onFile(File file) throws IOException {}

  /**
   * Notify the traversal has finished.
   */
  protected void onFinish() throws IOException {}

  /**
   * Gets the result to return for {@link #call()}.
   */
  protected V getResult() throws IOException {return null;}
}
