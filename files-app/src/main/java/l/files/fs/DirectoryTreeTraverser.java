package l.files.fs;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

/**
 * Traverses a directory tree and returns all the child paths.
 */
public abstract class DirectoryTreeTraverser<T extends DirectoryEntry>
    extends TreeTraverser<T> {

  /**
   * Calls {@link #preOrderIterator(Object)} with the given file converted to
   * an entry.
   *
   * @throws IllegalArgumentException if the given file type is not supported
   * @throws FileSystemException      if failed to create entry from given root
   */
  public abstract FluentIterable<T> preOrderTraversal(FileId root);

  /**
   * Calls {@link #postOrderTraversal(Object)} with the given file converted to
   * an entry.
   *
   * @throws IllegalArgumentException if the given file type is not supported
   * @throws FileSystemException      if failed to create entry from given root
   */
  public abstract FluentIterable<T> postOrderTraversal(FileId root);


  /**
   * Calls {@link #breadthFirstTraversal(Object)} with the given file converted
   * to an entry.
   *
   * @throws IllegalArgumentException if the given file type is not supported
   * @throws FileSystemException      if failed to create entry from given root
   */
  public abstract FluentIterable<T> breadthFirstTraversal(FileId root);

}
