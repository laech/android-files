package l.files.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.isSymlink;

/**
 * Traverses a set of root files in a breath first manner. Ignores any
 * symlinks.
 */
abstract class Traverser<V> implements Callable<V> {

  private final Set<File> roots;
  private final Cancellable listener;

  protected Traverser(Cancellable listener, Set<File> roots) {
    this.roots = checkNotNull(roots, "roots");
    this.listener = checkNotNull(listener, "listener");
  }

  @Override public final V call() throws IOException {
    Queue<File> queue = newLinkedList(roots);
    while (!queue.isEmpty()) {
      if (listener.isCancelled()) {
        return null;
      }

      File file = queue.poll();
      if (isSymlink(file)) {
        continue;
      }

      if (file.isDirectory()) {
        queue.addAll(getChildren(file));
        onDirectory(file);
      } else {
        onFile(file);
      }

      onFinish();
    }

    return getResult();
  }

  private List<File> getChildren(File dir) throws RestrictedException {
    File[] children = dir.listFiles();
    if (children == null) {
      throw new RestrictedException(dir);
    }
    return asList(children);
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
