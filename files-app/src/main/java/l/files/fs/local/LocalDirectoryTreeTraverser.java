package l.files.fs.local;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import l.files.fs.DirectoryEntry;
import l.files.fs.DirectoryTreeTraverser;
import l.files.fs.FileSystemException;
import l.files.fs.Path;
import l.files.logging.Logger;

import static java.util.Collections.emptyList;

public final class LocalDirectoryTreeTraverser extends DirectoryTreeTraverser {

  /*
   * Design note: traverses a directory tree, return a minimal entry structure
   * without additional file information (by calling stat/lstat) to ensure the
   * traversal is fast on large directories. Callers can then get file
   * information during traversal as needed outside of this class.
   */

  private static final Logger logger = Logger.get(LocalDirectoryTreeTraverser.class);

  private static final LocalDirectoryTreeTraverser instance =
      new LocalDirectoryTreeTraverser();

  private LocalDirectoryTreeTraverser() {}

  public static LocalDirectoryTreeTraverser get() {
    return instance;
  }

  @Override public Iterable<DirectoryEntry> children(DirectoryEntry root) {
    if (!((LocalDirectoryEntry) root).isDirectory()) {
      return emptyList();
    }

    try (LocalDirectoryStream stream = LocalDirectoryStream.open(root.path())) {
      return ImmutableList.copyOf(stream);
    } catch (FileSystemException e) {
      // TODO accept an exception handler as constructor parameter for this
      logger.warn(e);
      return emptyList();
    }
  }

  @Override public FluentIterable<DirectoryEntry> preOrderTraversal(Path root) {
    return preOrderTraversal(LocalDirectoryEntry.stat(root));
  }

  @Override public FluentIterable<DirectoryEntry> postOrderTraversal(Path root) {
    return postOrderTraversal(LocalDirectoryEntry.stat(root));
  }

  @Override public FluentIterable<DirectoryEntry> breadthFirstTraversal(Path root) {
    return breadthFirstTraversal(LocalDirectoryEntry.stat(root));
  }
}
