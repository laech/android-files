package l.files.fs.local;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.io.IOException;

import l.files.fs.PathEntry;
import l.files.fs.FileVisitor;
import l.files.fs.FileSystemException;
import l.files.fs.Path;
import l.files.logging.Logger;

import static java.util.Collections.emptyList;

public final class LocalFileVisitor extends FileVisitor {

  /*
   * Design note: traverses a directory tree, return a minimal entry structure
   * without additional file information (by calling stat/lstat) to ensure the
   * traversal is fast on large directories. Callers can then get file
   * information during traversal as needed outside of this class.
   */

  private static final Logger logger = Logger.get(LocalFileVisitor.class);

  private static final LocalFileVisitor instance = new LocalFileVisitor();

  private LocalFileVisitor() {}

  public static LocalFileVisitor get() {
    return instance;
  }

  @Override public Iterable<PathEntry> children(PathEntry root) {
    if (!((LocalPathEntry) root).isDirectory()) {
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

  @Override public FluentIterable<PathEntry> preOrderTraversal(Path root) throws IOException {
    return preOrderTraversal(LocalPathEntry.stat(root));
  }

  @Override public FluentIterable<PathEntry> postOrderTraversal(Path root) throws IOException {
    return postOrderTraversal(LocalPathEntry.stat(root));
  }

  @Override public FluentIterable<PathEntry> breadthFirstTraversal(Path root) throws IOException {
    return breadthFirstTraversal(LocalPathEntry.stat(root));
  }
}
