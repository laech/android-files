package l.files.fs;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

/**
 * Traverses a directory tree and returns all the child paths.
 * Will not follow symbolic links.
 */
public abstract class DirectoryTreeTraverser<T extends DirectoryEntry>
    extends TreeTraverser<T> {

  /**
   * Calls {@link #preOrderIterator(Object)} with the given file converted to
   * an entry.
   *
   * @throws IllegalArgumentException if the given file scheme is not supported
   * @throws FileSystemException      if failed to create entry from given root
   */
  public abstract FluentIterable<T> preOrderTraversal(Path root);

  /**
   * Calls {@link #postOrderTraversal(Object)} with the given file converted to
   * an entry.
   *
   * @throws IllegalArgumentException if the given file scheme is not supported
   * @throws FileSystemException      if failed to create entry from given root
   */
  public abstract FluentIterable<T> postOrderTraversal(Path root);


  /**
   * Calls {@link #breadthFirstTraversal(Object)} with the given file converted
   * to an entry.
   *
   * @throws IllegalArgumentException if the given file scheme is not supported
   * @throws FileSystemException      if failed to create entry from given root
   */
  public abstract FluentIterable<T> breadthFirstTraversal(Path root);

}
